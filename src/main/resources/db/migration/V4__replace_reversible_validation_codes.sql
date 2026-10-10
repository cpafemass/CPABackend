-- Preserva tokens legados para consulta, sem permitir sua reutilização.
alter table VALIDACAO drop constraint if exists VALIDACAO_pkey;
alter table VALIDACAO rename column HASH to OLD_TOKEN;
alter table VALIDACAO alter column OLD_TOKEN drop not null;
-- Registros legados permanecem sem NEW_HASH; novos códigos usam somente NEW_HASH.
alter table VALIDACAO add NEW_HASH uuid;
alter table VALIDACAO add column EXPIRA_EM timestamp(6);
update VALIDACAO set EXPIRA_EM = coalesce(DATA_CRIACAO, current_timestamp) + interval '14 days';
alter table VALIDACAO alter column EXPIRA_EM set not null;
