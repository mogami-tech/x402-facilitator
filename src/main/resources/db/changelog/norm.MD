# Naming norm
Table names:
- Use singular nouns (e.g., "User" instead of "Users").

Foreign key:
In the table, the foreign key column should be named: "FK_" + Reference table id column.
Example :
Table ADDRESS has a foreign key to PARTICIPANT, the foreign key column should be named: FK_PARTICIPANT_ID. The
foreign key name should be ADDRESS_FK_PARTICIPANT_ID
Table PAYMENT has several foreign keys to ADDRESS, the foreign key columns should be named: FK_FROM_ADDRESS_ID,
FK_FROM_ADDRESS_ID... the names should be PAYMENT_FK_FROM_ADDRESS_ID, PAYMENT_FK_TO_ADDRESS_ID...

TENANT_API_KEY