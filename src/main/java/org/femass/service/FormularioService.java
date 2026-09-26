package org.femass.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.femass.dto.CursoDTO;
import org.femass.dto.FormularioDTO;
import org.femass.dto.RespostaDTO;
import org.femass.dto.SubjectDTO;
import org.femass.entity.Avaliacao;
import org.femass.entity.Curso;
import org.femass.entity.Disciplina;
import org.femass.entity.Pergunta;
import org.femass.entity.Resposta;

import org.femass.entity.Validacao;
import org.femass.exception.CPFInvalidoException;
import org.femass.util.ValidacaoCPFUtil;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FormularioService {

    @Inject
    EntityManager entityManager;
    
    @Inject
    ValidacaoService validacaoService;



    @Inject
    QRCodeService qrCodeService;

    @Transactional
    public void salvar(FormularioDTO formularioDTO) {
        salvarFormulario(formularioDTO);
    }

    @Transactional
    public Validacao salvarEGerarHash(FormularioDTO formularioDTO) {
        salvarFormulario(formularioDTO);
        String codigoValidacao = qrCodeService.codificar(formularioDTO);
        return validacaoService.armazenarCodigoValidacao(
                codigoValidacao,
                formularioDTO.respondent.aceiteTermosCondicoesServico
        );
    }

    private void salvarFormulario(FormularioDTO formularioDTO) {
        validarFormulario(formularioDTO);

        int avaliacoesSalvas = 0;

        if (formularioDTO == null) {
            throw new CPFInvalidoException("Formulário não pode ser nulo");
        }
        // Validar CPF do respondente (lança CPFInvalidoException em caso de problemas)
        validaCPF(formularioDTO);

        Curso curso = buscarOuCriarCurso(formularioDTO.course);
        
        if (curso == null) {
            return;
        }
        if (formularioDTO.subjects == null) {
            return;
        }


        // First pass: resolve all disciplinas. If any subject references a
        // disciplina that does not exist in the domain table, abort and return
        // a clear error (we don't want partial persistence).
        List<String> missing = new ArrayList<>();
        List<Disciplina> disciplinas = new ArrayList<>();
        for (SubjectDTO subjectDTO : formularioDTO.subjects) {
            validarSubject(subjectDTO);

            Disciplina disciplina = buscarOuCriarDisciplina(subjectDTO, curso);

            if (disciplina == null) {
                String idOrName = subjectDTO.subjectId != null ? subjectDTO.subjectId : subjectDTO.subjectName;
                missing.add(idOrName == null ? "(sem identificador)" : idOrName);
            }
            disciplinas.add(disciplina);
        }

        if (!missing.isEmpty()) {
            // Throw a domain-level exception so the resource can return 400.
            throw new org.femass.exception.InvalidFormularioException("Disciplinas não encontradas: " + String.join(", ", missing));
        }

        // Second pass: now that all disciplinas are resolved, create e persistir avaliações
        for (int i = 0; i < formularioDTO.subjects.size(); i++) {
            SubjectDTO subjectDTO = formularioDTO.subjects.get(i);
            Disciplina disciplina = disciplinas.get(i);


            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setDisciplina(disciplina);
            avaliacao.setComentariosGerais(subjectDTO.comment);

            List<Resposta> respostas = new ArrayList<>();
            for (RespostaDTO respostaDTO : subjectDTO.answers) {
                validarResposta(respostaDTO);

                Pergunta pergunta = buscarOuCriarPergunta(respostaDTO);
                Resposta resposta = new Resposta();
                resposta.setAvaliacao(avaliacao);
                resposta.setPergunta(pergunta);
                resposta.setNota(respostaDTO.score);
                respostas.add(resposta);
            }

            avaliacao.setRespostas(respostas);
            entityManager.persist(avaliacao);
            avaliacoesSalvas++;
        }

        if (avaliacoesSalvas == 0) {
            throw new IllegalArgumentException("Nenhuma avaliacao valida foi informada");
        }

        entityManager.flush();
    }

    private void validarFormulario(FormularioDTO formularioDTO) {
        if (formularioDTO == null) {
            throw new IllegalArgumentException("Formulario nao pode ser vazio");
        }

        if (formularioDTO.course == null) {
            throw new IllegalArgumentException("Curso e obrigatorio");
        }

        if (formularioDTO.course.name == null || formularioDTO.course.name.isBlank()) {
            throw new IllegalArgumentException("Nome do curso e obrigatorio");
        }

        if (formularioDTO.respondent == null) {
            throw new IllegalArgumentException("Dados do respondente sao obrigatorios");
        }

        if (formularioDTO.respondent.cpf == null || formularioDTO.respondent.cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do respondente e obrigatorio");
        }

        if (apenasDigitos(formularioDTO.respondent.cpf).length() < 4) {
            throw new IllegalArgumentException("CPF do respondente deve ter ao menos 4 digitos");
        }

        if (formularioDTO.respondent.matricula == null || formularioDTO.respondent.matricula.isBlank()) {
            throw new IllegalArgumentException("Matricula do respondente e obrigatoria");
        }

        if (!Boolean.TRUE.equals(formularioDTO.respondent.aceiteTermosCondicoesServico)) {
            throw new IllegalArgumentException("Aceite dos termos e condicoes de servico e obrigatorio");
        }

        if (formularioDTO.subjects == null || formularioDTO.subjects.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma disciplina deve ser informada");
        }
    }

    private void validarSubject(SubjectDTO subjectDTO) {
        if (subjectDTO == null) {
            throw new IllegalArgumentException("Disciplina nao pode ser vazia");
        }

        if (subjectDTO.subjectName == null || subjectDTO.subjectName.isBlank()) {
            throw new IllegalArgumentException("Nome da disciplina e obrigatorio");
        }

        if (subjectDTO.teacherName == null || subjectDTO.teacherName.isBlank()) {
            throw new IllegalArgumentException("Nome do professor e obrigatorio");
        }

        if (subjectDTO.answers == null || subjectDTO.answers.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma resposta deve ser informada");
        }
    }

    private void validarResposta(RespostaDTO respostaDTO) {
        if (respostaDTO == null) {
            throw new IllegalArgumentException("Resposta nao pode ser vazia");
        }

        if (respostaDTO.questionText == null || respostaDTO.questionText.isBlank()) {
            throw new IllegalArgumentException("Texto da pergunta e obrigatorio");
        }

        if (respostaDTO.score == null) {
            throw new IllegalArgumentException("Nota da resposta e obrigatoria");
        }

        if (respostaDTO.score < 1 || respostaDTO.score > 5) {
            throw new IllegalArgumentException("Nota da resposta deve estar entre 1 e 5");
        }
    }
     private void validaCPF(FormularioDTO formularioDTO) {
        // Validar CPF do respondente
         if (formularioDTO.respondent == null) {
             throw new CPFInvalidoException("Respondente não pode ser nulo");
         }

         if (formularioDTO.respondent.cpf == null || formularioDTO.respondent.cpf.isBlank()) {
             throw new CPFInvalidoException("CPF do respondente não pode ser vazio");
         }

         if (!ValidacaoCPFUtil.validarCPF(formularioDTO.respondent.cpf)) {
             throw new CPFInvalidoException("CPF inválido: " + formularioDTO.respondent.cpf);
         }
     }

    private Curso buscarOuCriarCurso(CursoDTO cursoDTO) {
        Curso curso = entityManager.createQuery(
                "from Curso where nome = :nome", Curso.class)
            .setParameter("nome", cursoDTO.name)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (curso == null) {
            curso = new Curso();
            curso.setNome(cursoDTO.name);
            entityManager.persist(curso);
        }

        return curso;
    }

    private Disciplina buscarOuCriarDisciplina(SubjectDTO subjectDTO, Curso curso) {
        if (subjectDTO == null || curso == null) {
            return null;
        }

        if (subjectDTO.subjectName == null || subjectDTO.subjectName.isBlank()) {
            return null;
        }

        if (subjectDTO.teacherName == null || subjectDTO.teacherName.isBlank()) {
            return null;
        }

        // Prefer lookup by subjectId when provided. If subjectId is numeric, try to
        // find by primary key. If not numeric or not found, fall back to name+professor lookup.
        Disciplina disciplina = null;

        if (subjectDTO.subjectId != null && !subjectDTO.subjectId.isBlank()) {
            try {
                Long did = Long.parseLong(subjectDTO.subjectId);
                disciplina = entityManager.find(Disciplina.class, did);
                if (disciplina != null) {
                    // verify it belongs to the same course
                    if (disciplina.getCurso() == null || disciplina.getCurso().getId() == null || !disciplina.getCurso().getId().equals(curso.getId())) {
                        disciplina = null;
                    }
                }
            } catch (NumberFormatException nfe) {
                // subjectId is not a number; fallthrough to name search below
            }
        }

        if (disciplina == null && subjectDTO.teacherName != null && !subjectDTO.teacherName.isBlank()) {
            disciplina = entityManager.createQuery(
                    "from Disciplina where id = :id",
                    Disciplina.class)
                .setParameter("id", subjectDTO.subjectId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        }

        // If not found, return null so the caller will skip this subject.
        return disciplina;
    }

    private Pergunta buscarOuCriarPergunta(RespostaDTO respostaDTO) {
        Pergunta pergunta = null;

        if (respostaDTO.questionId != null && !respostaDTO.questionId.isBlank()) {
            pergunta = entityManager.createQuery(
                    "from Pergunta where codigo = :codigo", Pergunta.class)
                .setParameter("codigo", respostaDTO.questionId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        }

        if (pergunta == null) {
            pergunta = entityManager.createQuery(
                    "from Pergunta where texto = :texto", Pergunta.class)
                .setParameter("texto", respostaDTO.questionText)
                .getResultStream()
                .findFirst()
                .orElse(null);
        }

        if (pergunta == null) {
            pergunta = new Pergunta();
            pergunta.setTexto(respostaDTO.questionText);
            entityManager.persist(pergunta);
        }

        return pergunta;
    }

    private String primeirosQuatroDigitosCpf(String cpf) {
        return apenasDigitos(cpf).substring(0, 4);
    }

    private String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
