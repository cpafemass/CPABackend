-- Códigos legados continham payload pessoal reversível; invalidá-los é obrigatório.
delete from VALIDACAO;
alter table VALIDACAO alter column HASH varchar(64) not null;
