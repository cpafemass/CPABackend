-- Códigos legados continham payload pessoal reversível; invalidá-los é obrigatório.
delete from VALIDACAO;
alter table VALIDACAO drop constraint if exists VALIDACAO_pkey;
alter table VALIDACAO rename column HASH to CODIGO_DIGEST;
alter table VALIDACAO alter column CODIGO_DIGEST type varchar(64);
alter table VALIDACAO alter column CODIGO_DIGEST set not null;
alter table VALIDACAO add primary key (CODIGO_DIGEST);
alter table VALIDACAO add column EXPIRA_EM timestamp(6);
update VALIDACAO set EXPIRA_EM = coalesce(DATA_CRIACAO, current_timestamp) + interval '14 days';
alter table VALIDACAO alter column EXPIRA_EM set not null;
