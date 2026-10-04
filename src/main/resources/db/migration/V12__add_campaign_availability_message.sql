ALTER TABLE campanha
    ADD COLUMN IF NOT EXISTS mensagem_disponibilidade VARCHAR(500);

UPDATE campanha
SET mensagem_disponibilidade = CASE estado
    WHEN 'ABERTA' THEN 'Campanha disponível para respostas.'
    WHEN 'ENCERRADA' THEN 'Campanha encerrada para respostas.'
    WHEN 'APROVADA' THEN 'Campanha aprovada, aguardando abertura.'
    ELSE 'Campanha ainda não está disponível para respostas.'
END
WHERE mensagem_disponibilidade IS NULL;
