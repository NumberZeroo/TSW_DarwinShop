DROP DATABASE IF EXISTS tswproj;
CREATE DATABASE tswproj;
USE tswproj;

CREATE TABLE Utente (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    imgPath VARCHAR(255) NOT NULL,
    isAdmin BIGINT NOT NULL,
    password CHAR(128) NOT NULL, # SHA-512
    CONSTRAINT fk_utente_admin CHECK (isAdmin IN (0, 1))
);

CREATE TABLE Ordine (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    idUtente BIGINT UNSIGNED NOT NULL,
    pathFattura VARCHAR(255) NOT NULL,
    FOREIGN KEY (idUtente)
        REFERENCES Utente (id)
);

CREATE TABLE Animale (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	Nome VARCHAR(255) NOT NULL
);

CREATE TABLE Pet (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(255) NOT NULL,
    IdUtente BIGINT UNSIGNED NOT NULL,
    imgPath VARCHAR(255) NOT NULL,
    Tipo BIGINT UNSIGNED NOT NULL,
    Taglia ENUM('PICCOLA', 'MEDIA', 'GRANDE') NULL,
    Sterilizzato CHAR(255) NULL,
    DataNascita DATE NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(IdUtente) REFERENCES Utente(id),
    FOREIGN KEY(Tipo) REFERENCES Animale(id)
);

CREATE TABLE Prodotto (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nome VARCHAR(255) NOT NULL,
    Disponibilità TINYINT(1) NOT NULL,
    Taglia ENUM('PICCOLA', 'MEDIA', 'GRANDE') NULL,
    Categoria VARCHAR(255) NOT NULL,
    TipoAnimale BIGINT UNSIGNED NOT NULL,
    MinEta INT NULL,
    MaxEta INT NULL,
    IVA ENUM('4', '10', '22') NOT NULL,
    Prezzo BIGINT NOT NULL,
    Sterilizzati TINYINT(1) NULL,
    imgPath VARCHAR(255) NOT NULL,
    FOREIGN KEY(TipoAnimale) REFERENCES Animale(id)
);

CREATE TABLE Carrello (
    IdUtente BIGINT UNSIGNED NOT NULL,
    idProdotto BIGINT UNSIGNED NOT NULL,
    Quantita BIGINT NOT NULL,
    PRIMARY KEY(IdUtente, idProdotto),
    FOREIGN KEY(IdUtente) REFERENCES Utente(id),
    FOREIGN KEY(idProdotto) REFERENCES Prodotto(id)
);

CREATE TABLE OrderItem (
    idItem BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    IdOrdine BIGINT UNSIGNED NOT NULL,
    Prezzo BIGINT NOT NULL,
    Quantita BIGINT NOT NULL,
    FOREIGN KEY(IdOrdine) REFERENCES Ordine(id)
);

CREATE TABLE KartItem (
   idItem BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   IdCarrello BIGINT UNSIGNED NOT NULL,
   Quantita BIGINT NOT NULL,
   FOREIGN KEY(IdCarrello) REFERENCES Ordine(id)
);

CREATE TABLE Recensione (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    idUtente BIGINT UNSIGNED NOT NULL,
    Titolo VARCHAR(255) NOT NULL,
    Commento VARCHAR(255) NOT NULL,
    Valutazione BIGINT NOT NULL,
    Data DATE NOT NULL,
    idProdotto BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY(id, idUtente),
    FOREIGN KEY(idUtente) REFERENCES Utente(id),
    FOREIGN KEY(idProdotto) REFERENCES Prodotto(id)
);
