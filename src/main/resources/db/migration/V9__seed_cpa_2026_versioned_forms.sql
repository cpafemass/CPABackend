DO $$
<<seed>>
DECLARE
    campanha_id BIGINT;
    formulario_id BIGINT;
    versao_id BIGINT;
    pergunta_id BIGINT;
    f RECORD;
    i INTEGER;
    textos TEXT[];
BEGIN
    INSERT INTO campanha (codigo, nome, ativa)
    VALUES ('cpa-2026', 'CPA 2026', TRUE)
    ON CONFLICT (codigo) DO UPDATE SET nome = EXCLUDED.nome, ativa = EXCLUDED.ativa
    RETURNING id INTO campanha_id;
    IF campanha_id IS NULL THEN SELECT id INTO campanha_id FROM campanha WHERE codigo = 'cpa-2026'; END IF;

    FOR f IN SELECT * FROM (VALUES
        ('discente_disciplinas','Discentes - avaliacao das disciplinas','ALUNO',FALSE,ARRAY['Promove o debate e instiga o pensamento crítico, colaborando para a autonomia dos estudantes.','Relaciona aspectos teóricos com suas implicações e aplicações práticas.','Há coerência entre o que é ensinado e o que se solicita nas avaliações.','Utiliza diferentes estratégias avaliativas.','Relaciona a sua disciplina com conteúdos, competências e habilidades de outras disciplinas do curso.','Analisa os resultados das avaliações com os estudantes, desenvolvendo mecanismos para a superação das dificuldades.','Mostra-se aberto ao diálogo, motivando os discentes a prosseguir nos estudos.','Faz uso adequado do seu tempo regular de aula, facilitando a aprendizagem em sala de aula.','Apresentou a disciplina disponibilizando plano de curso, estratégias de ensino e critérios de avaliação.','Utiliza diferentes estratégias pedagógicas que incentivam a aprendizagem e a pesquisa.']),
        ('docente_disciplinas','Docentes - autoavaliacao das disciplinas','PROFESSOR',TRUE,ARRAY['Promove o debate e instiga o pensamento crítico, colaborando para a autonomia dos estudantes.','Relaciona aspectos teóricos com suas implicações e aplicações práticas.','Há coerência entre o que é ensinado e o que se solicita nas avaliações.','Utiliza diferentes estratégias avaliativas.','Relaciona a sua disciplina com conteúdos, competências e habilidades de outras disciplinas do curso.','Analisa os resultados das avaliações com os estudantes, desenvolvendo mecanismos para a superação das dificuldades.','Mostra-se aberto ao diálogo, motivando os discentes a prosseguir nos estudos.','Faz uso adequado do seu tempo regular de aula, facilitando a aprendizagem em sala de aula.','Apresentou a disciplina disponibilizando plano de curso, estratégias de ensino e critérios de avaliação.','Utiliza diferentes estratégias pedagógicas que incentivam a aprendizagem e a pesquisa.']),
        ('discente_experiencia','Discentes - autoavaliacao da experiencia academica','ALUNO',FALSE,ARRAY['Estabeleço uma relação participativa, de respeito e cordialidade com os membros da Comunidade Acadêmica.','Permaneço nas aulas durante o período estabelecido (do início ao fim das aulas).','Participo ativamente das atividades propostas.','Dedico-me para aprender os conteúdos ministrados.','Dedico-me para buscar conhecimentos adicionais fora do espaço da sala de aula.','Acesso as ferramentas necessárias, como o AVA, por exemplo, para buscar conteúdos e informações sobre procedimentos da vida acadêmica.']),
        ('discente_gestao','Discentes - avaliacao da gestao','ALUNO',FALSE,ARRAY['A direção atua de forma efetiva na busca de soluções para as demandas dos estudantes.','A direção apresenta respostas aos encaminhamentos do diretório acadêmico representando as demandas dos estudantes.','A direção esteve presente, atuando nos diversos espaços da instituição durante esse semestre.','A direção é aberta à comunicação, apresentando agilidade na resposta às questões técnicas e pedagógicas.','A direção apresentou iniciativa, agilidade e proatividade nas atividades de sua atuação.','A coordenação do seu curso é aberta à comunicação, apresentando agilidade na resposta a questões técnicas e pedagógicas.','A coordenação se faz presente na rotina acadêmica, detectando problemas e promovendo soluções efetivas nas atividades.','A coordenação de seu curso promoveu e incentivou a participação em atividades acadêmicas.','A coordenação tem atuação dinâmica na proposição de atividades de pesquisa e extensão para o curso.','A coordenação mantém um fluxo de informações entre o Conselho de Curso e os discentes.']),
        ('discente_instituicao','Discentes - avaliacao da instituicao','ALUNO',FALSE,ARRAY['Os equipamentos e materiais disponíveis para as aulas práticas são adequados para a quantidade de estudantes.','A instituição disponibiliza recursos para o pleno funcionamento das aulas.','A instituição disponibiliza equipes e horários adequados para atender solicitações relativas à documentação e processos na secretaria.','A instituição promove manutenção, conservação e limpeza das instalações físicas em condições satisfatórias.','A instituição tem organização de seus processos e espaços promovendo segurança e acessibilidade aos discentes.','A FeMASS divulga em seu site e outros canais, como o AVA, a Missão e projetos institucionais.','A instituição participa de campanhas socioeducativas, ações de responsabilidade ambiental e projetos culturais que atendam à comunidade acadêmica.']),
        ('docente_gestao','Docentes - avaliacao da gestao','PROFESSOR',TRUE,ARRAY['A gestão demonstra comprometimento com a qualidade do ensino.','A gestão promove reuniões para a organização e o planejamento das atividades acadêmicas.','A gestão promove e divulga cursos de formação pedagógica.','A gestão promove divulgação efetiva da FeMASS para o público externo.','A gestão promove um canal de escuta e diálogo sobre as demandas acadêmicas.','A gestão avalia periodicamente a eficiência dos processos de trabalho praticados na instituição.','A gestão valoriza o trabalho dos professores.','A gestão aceita críticas e sugestões a respeito da sua gestão, valorizando ideias inovadoras de sua equipe e dos colaboradores.']),
        ('docente_instituicao','Docentes - avaliacao da instituicao','PROFESSOR',TRUE,ARRAY['A Instituição oferece condições adequadas de segurança e acesso, inclusive para portadores de necessidades especiais?','O ambiente para as aulas é apropriado quanto à acústica, luminosidade e ventilação?','A manutenção, conservação e limpeza das instalações físicas são satisfatórias?','Os laboratórios são adequados em termos de espaço e equipamento.']),
        ('funcionario_auto','Funcionarios - autoavaliacao','FUNCIONARIO',FALSE,ARRAY['Promovo a integração entre os setores administrativos e pedagógicos.','Sinto que meu trabalho é valorizado.','Demonstro proatividade no cumprimento das minhas funções.','Atuo de forma colaborativa.','Estabeleço um relacionamento ético com os membros da comunidade acadêmica.','Busco aperfeiçoamento acadêmico e/ou profissional.']),
        ('funcionario_gestao','Funcionarios - avaliacao da gestao','FUNCIONARIO',FALSE,ARRAY['A gestão acompanha as atividades específicas de cada setor.','A gestão promove um canal de escuta e diálogo sobre as questões acadêmicas.','A gestão fornece as ferramentas necessárias para a realização das suas atividades profissionais.','Há um ambiente profissional ético e saudável.','A gestão valoriza o trabalho dos funcionários.']),
        ('funcionario_instituicao','Funcionarios - avaliacao da instituicao','FUNCIONARIO',FALSE,ARRAY['Existe na Instituição um ambiente profissional de satisfação na realização da função administrativa, pois a instituição valoriza esse profissional.','A FeMASS mantém boas condições de trabalho que geram satisfação do corpo técnico-administrativo.','Na FeMASS os técnicos são encorajados ao engajamento em atividades que promovam o trabalho acadêmico produzido na Instituição.','A FeMASS tem uma imagem positiva na comunidade.','A instituição disponibiliza e divulga o PDI atualizado.','O corpo técnico-administrativo é informado sobre as atividades que deverão ser executadas com antecedência.']),
        ('gestao_auto','Gestao - autoavaliacao','GESTAO',TRUE,ARRAY['Tenho conhecimento das rotinas e processos de trabalho pertinentes ao cargo e sei executá-los com competência.','Acompanho as atividades dos setores, procurando colaborar com sugestões, para corrigir possíveis desvios em relação às metas e objetivos planejados.','Defino objetivamente os resultados a serem alcançados pela gestão e pelos colaboradores, acentuando como a função de cada um contribui para o alcance dos objetivos do setor.','Exijo que o setor cumpra os prazos de entrega dos trabalhos e procuro incentivar e organizar a equipe para atender demandas emergenciais.','Aceito críticas e sugestões a respeito da gestão, valorizando ideias inovadoras da equipe e dos colaboradores.','Atuo de forma coerente com o discurso e valores defendidos.','Promovo reuniões regulares para discutir os projetos, processos e problemas que afetam a gestão, reorientando as ações necessárias para melhoria dos processos, definindo atividades, responsabilidades e prazos para garantir as metas.','Avalio periodicamente a eficiência dos processos de trabalho praticados na instituição.'])
    ) AS x(codigo,nome,publico,comentario,textos) LOOP
        INSERT INTO formulario (codigo,nome,publico,campanha_id) VALUES (f.codigo,f.nome,f.publico,seed.campanha_id)
        ON CONFLICT ON CONSTRAINT uk_formulario_campanha_codigo DO UPDATE SET nome=EXCLUDED.nome, publico=EXCLUDED.publico
        RETURNING id INTO formulario_id;
        IF formulario_id IS NULL THEN SELECT fm.id INTO formulario_id FROM formulario fm WHERE fm.campanha_id=seed.campanha_id AND fm.codigo=f.codigo; END IF;
        INSERT INTO formulario_versao (formulario_id,numero,comentario_permitido) VALUES (seed.formulario_id,1,f.comentario)
        ON CONFLICT ON CONSTRAINT uk_formulario_versao DO UPDATE SET comentario_permitido=EXCLUDED.comentario_permitido
        RETURNING id INTO versao_id;
        IF versao_id IS NULL THEN SELECT fv.id INTO versao_id FROM formulario_versao fv WHERE fv.formulario_id=seed.formulario_id AND fv.numero=1; END IF;
        textos := f.textos;
        FOR i IN 1..array_length(textos,1) LOOP
            INSERT INTO pergunta_versao (formulario_versao_id,codigo,texto,ordem) VALUES (seed.versao_id,'q'||i,textos[i],i)
            ON CONFLICT ON CONSTRAINT uk_pergunta_versao DO UPDATE SET texto=EXCLUDED.texto,ordem=EXCLUDED.ordem
            RETURNING id INTO pergunta_id;
            IF pergunta_id IS NULL THEN SELECT id INTO pergunta_id FROM pergunta_versao WHERE formulario_versao_id=versao_id AND codigo='q'||i; END IF;
            INSERT INTO opcao_pergunta (pergunta_versao_id,codigo,rotulo,valor,nao_sei_responder,ordem) VALUES
              (seed.pergunta_id,'discordo_totalmente','Discordo totalmente',1,FALSE,1),
              (seed.pergunta_id,'discordo_parcialmente','Discordo parcialmente',2,FALSE,2),
              (seed.pergunta_id,'nao_sei_responder','Não sei responder',NULL,TRUE,3),
              (seed.pergunta_id,'concordo_parcialmente','Concordo parcialmente',4,FALSE,4),
              (seed.pergunta_id,'concordo_totalmente','Concordo totalmente',5,FALSE,5)
            ON CONFLICT ON CONSTRAINT uk_opcao_pergunta DO UPDATE SET rotulo=EXCLUDED.rotulo,valor=EXCLUDED.valor,nao_sei_responder=EXCLUDED.nao_sei_responder,ordem=EXCLUDED.ordem;
        END LOOP;
    END LOOP;

    INSERT INTO formulario (codigo,nome,publico,campanha_id) VALUES ('legado','Formulario legado','ALUNO',seed.campanha_id)
    ON CONFLICT ON CONSTRAINT uk_formulario_campanha_codigo DO NOTHING;
    SELECT fm.id INTO formulario_id FROM formulario fm WHERE fm.campanha_id=seed.campanha_id AND fm.codigo='legado';
    INSERT INTO formulario_versao (formulario_id,numero,comentario_permitido) VALUES (seed.formulario_id,1,TRUE)
    ON CONFLICT ON CONSTRAINT uk_formulario_versao DO NOTHING;
    SELECT fv.id INTO versao_id FROM formulario_versao fv WHERE fv.formulario_id=seed.formulario_id AND fv.numero=1;
    FOR f IN SELECT id,codigo,texto FROM pergunta WHERE codigo IS NOT NULL ORDER BY id LOOP
        INSERT INTO pergunta_versao (formulario_versao_id,codigo,texto,ordem) VALUES (seed.versao_id,f.codigo,f.texto,regexp_replace(f.codigo,'[^0-9]','','g')::INTEGER)
        ON CONFLICT ON CONSTRAINT uk_pergunta_versao DO NOTHING;
    END LOOP;
    UPDATE avaliacao SET formulario_versao_id=seed.versao_id WHERE formulario_versao_id IS NULL;
END $$;
