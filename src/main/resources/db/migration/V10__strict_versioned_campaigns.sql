ALTER TABLE campanha ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO';

ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS publico VARCHAR(20);
ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS nome VARCHAR(180);
ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS ordem INTEGER;
ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS escopo VARCHAR(20) NOT NULL DEFAULT 'GERAL';
ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS aviso_comentario VARCHAR(500);
ALTER TABLE formulario_versao ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO';

UPDATE formulario_versao fv
SET publico = f.publico,
    nome = f.nome,
    ordem = COALESCE(fv.ordem, 1),
    escopo = CASE WHEN f.codigo IN ('discente_disciplinas', 'docente_disciplinas', 'legado') THEN 'DISCIPLINA' ELSE 'GERAL' END,
    aviso_comentario = CASE WHEN fv.comentario_permitido THEN 'Comentário opcional, anônimo e tratado somente de forma agregada.' ELSE NULL END,
    estado = 'PUBLICADA'
FROM formulario f
WHERE fv.formulario_id = f.id;

-- A campanha inicial já corresponde ao catálogo aprovado que estava em produção.
UPDATE campanha SET estado = 'ABERTA' WHERE codigo = 'cpa-2026';

ALTER TABLE avaliacao ALTER COLUMN disciplina_id DROP NOT NULL;
ALTER TABLE resposta ADD COLUMN IF NOT EXISTS opcao_rotulo VARCHAR(100);

-- Completa o catálogo legado para que respostas anteriores recebam um snapshot de pergunta.
INSERT INTO pergunta_versao (formulario_versao_id, codigo, texto, ordem)
SELECT fv.id, COALESCE(NULLIF(p.codigo, ''), 'legado-' || p.id), p.texto, p.id
FROM formulario_versao fv
JOIN formulario f ON f.id = fv.formulario_id AND f.codigo = 'legado'
JOIN pergunta p ON TRUE
ON CONFLICT ON CONSTRAINT uk_pergunta_versao DO NOTHING;

INSERT INTO opcao_pergunta (pergunta_versao_id, codigo, rotulo, valor, nao_sei_responder, ordem)
SELECT pv.id, x.codigo, x.rotulo, x.valor, FALSE, x.ordem
FROM pergunta_versao pv
JOIN formulario_versao fv ON fv.id = pv.formulario_versao_id
JOIN formulario f ON f.id = fv.formulario_id AND f.codigo = 'legado'
CROSS JOIN (VALUES
    ('legado-1', 'Resposta legada: 1', 1, 1),
    ('legado-2', 'Resposta legada: 2', 2, 2),
    ('legado-3', 'Resposta legada: 3', 3, 3),
    ('legado-4', 'Resposta legada: 4', 4, 4),
    ('legado-5', 'Resposta legada: 5', 5, 5)
) AS x(codigo, rotulo, valor, ordem)
ON CONFLICT ON CONSTRAINT uk_opcao_pergunta DO NOTHING;

UPDATE resposta r
SET pergunta_versao_id = pv.id,
    opcao_codigo = COALESCE(r.opcao_codigo, 'legado-' || r.nota),
    opcao_rotulo = COALESCE(r.opcao_rotulo, 'Resposta legada: ' || r.nota)
FROM pergunta_versao pv
JOIN formulario_versao fv ON fv.id = pv.formulario_versao_id
JOIN formulario f ON f.id = fv.formulario_id AND f.codigo = 'legado'
JOIN pergunta p ON TRUE
WHERE r.pergunta_versao_id IS NULL
  AND p.id = r.pergunta_id
  AND pv.codigo = COALESCE(NULLIF(p.codigo, ''), 'legado-' || p.id);

UPDATE formulario_versao fv
SET comentario_permitido = FALSE,
    aviso_comentario = NULL
FROM formulario f
WHERE f.id = fv.formulario_id
  AND f.codigo = 'gestao_auto';
