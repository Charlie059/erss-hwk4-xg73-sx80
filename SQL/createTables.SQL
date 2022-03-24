CREATE TABLE Accounts (
                          Account_id SERIAL PRIMARY KEY,
                          Balance real NOT NULL CONSTRAINT positive_balance CHECK (Balance >= 0)
);

CREATE TABLE Positions (
                           Position_id SERIAL PRIMARY KEY,
                           Symbol varchar(10) NOT NULL,
                           Amount real NOT NULL CONSTRAINT positive_amount CHECK (Amount >= 0),
                           Account_id int NOT NULL,
                           FOREIGN KEY(Account_id) REFERENCES Accounts(Account_id) ON DELETE CASCADE
);

CREATE TYPE Status_enum AS ENUM ('OPEN', 'CANCELLED','EXECUTED');
CREATE TABLE Orders (
                        Order_id SERIAL PRIMARY KEY,
                        Trans_id int NOT NULL,
                        Account_id int NOT NULL,
                        Symbol varchar(10) NOT NULL,
                        Amount real NOT NULL,
                        Limit_price real NOT NULL CONSTRAINT positive_price CHECK (Limit_price >= 0),
                        Status Status_enum NOT NULL DEFAULT 'OPEN',
                        FOREIGN KEY(Account_id) REFERENCES Accounts(Account_id) ON DELETE CASCADE
);