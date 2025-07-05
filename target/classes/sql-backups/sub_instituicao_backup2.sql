-- H2 2.1.214;

-- 6 +/- SELECT COUNT(*) FROM PUBLIC.SUB_INSTITUICAO;         
INSERT INTO "PUBLIC"."SUB_INSTITUICAO" VALUES
(DATE '2025-07-02', 1, 1, 'Subinstituicao Aurora 1 1', 'A'),
(DATE '2025-07-02', 2, 1, 'Subinstituicao Aurora 1 2', 'A'),
(DATE '2025-07-02', 3, 1, 'Subinstituicao Aurora 1 3', 'A'),
(DATE '2025-07-02', 4, 2, 'Subinstituicao Luz 2 4', 'A'),
(DATE '2025-07-02', 5, 2, 'Subinstituicao Luz 2 5', 'A'),
(DATE '2025-07-02', 6, 3, 'Subinstituicao Cuz 3 6', 'A');      
ALTER TABLE "PUBLIC"."SUB_INSTITUICAO" ADD CONSTRAINT "PUBLIC"."FKDC00WS2P2QJ1H8IA38F4F19C1" FOREIGN KEY("ID_INSTITUICAO") REFERENCES "PUBLIC"."INSTITUICAO"("ID") NOCHECK;   
