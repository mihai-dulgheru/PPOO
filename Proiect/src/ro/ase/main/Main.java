package ro.ase.main;

import ro.ase.abstractClasses.Cititor;
import ro.ase.abstractClasses.Meniu;
import ro.ase.classes.*;
import ro.ase.constants.CaiFisiere;
import ro.ase.enums.Categorie;
import ro.ase.enums.TipCititor;
import ro.ase.exceptions.*;
import ro.ase.util.Data;
import ro.ase.util.Fisier;

import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String lineSeparator = System.lineSeparator();
    private static final Raport raport = new Raport();
    private static Biblioteca biblioteca = null;
    private static Cititor cititor = null;
    private static Imprumut imprumut = null;
    private static ListaCarti listaCarti = null;
    private static ListaCititori listaCititori = null;
    private static ListaImprumuturi listaImprumuturi = null;
    private static RegistruCarti registruCarti = null;
    private static int optiune = 0;

    public static void main(String[] args) {
        incarcareDate();

        new MeniuCont().afiseazaMeniu();
        citesteOptiunea();
        if (optiune == 0) {
            iesire();
        }
        Meniu meniu = null;
        switch (optiune) {
            case 1 -> {
                meniu = new MeniuBibliotecar();
                while (optiune != 0) {
                    meniu.afiseazaMeniu();
                    citesteOptiunea();
                    switch (optiune) {
                        case 1 -> adaugaCarteInColectie();
                        case 2 -> stergeCarteDinColectie();
                        case 3 -> genereazaRaportAutor();
                        case 4 -> genereazaRaportColectie();
                        case 5 -> genereazaRaportData();
                        case 0 -> iesire();
                        default -> optiuneInvalida();
                    }
                }
            }
            case 2 -> {
                meniu = new MeniuCititor();
                while (optiune != 0) {
                    meniu.afiseazaMeniu();
                    citesteOptiunea();
                    switch (optiune) {
                        case 1 -> inregistreazaCititor();
                        case 2 -> imprumutaCarte();
                        case 3 -> returneazaCarte();
                        case 0 -> iesire();
                        default -> optiuneInvalida();
                    }
                }
            }
            default -> optiuneInvalida();
        }
    }

    static void incarcareDate() {
        try {
            biblioteca = Biblioteca.fromFile(Fisier.citesteDinFisier(CaiFisiere.BIBLIOTECA));
            listaCarti = ListaCarti.fromFile(Fisier.citesteDinFisier(CaiFisiere.LISTA_CARTI));
            listaCititori = ListaCititori.fromFile(Fisier.citesteDinFisier(CaiFisiere.LISTA_CITITORI));
            listaImprumuturi = ListaImprumuturi.fromFile(Fisier.citesteDinFisier(CaiFisiere.LISTA_IMPRUMUTURI));
            registruCarti = RegistruCarti.fromFile(Fisier.citesteDinFisier(CaiFisiere.REGISTRU_CARTI));
        } catch (ExceptieIncarcareDateDinFisier e) {
            e.printStackTrace();
        }
    }

    static void salveazaDate() {
        biblioteca.salveazaInFisier();
        listaCarti.salveazaInFisier();
        listaCititori.salveazaInFisier();
        listaImprumuturi.salveazaInFisier();
        registruCarti.salveazaInFisier();
    }

    static void adaugaCarteInColectie() {
        try {
            golesteBufferul();
            System.out.print("Titlu: ");
            String titlu = scanner.nextLine().trim();
            System.out.print("Autor: ");
            String autor = scanner.nextLine().trim();
            System.out.print("Editura: ");
            String editura = scanner.nextLine().trim();
            System.out.print("An apari??ie: ");
            int anAparitie = scanner.nextInt();
            System.out.print("Num??r pagini: ");
            int nrPagini = scanner.nextInt();
            golesteBufferul();
            System.out.print("Categorie: ");
            String categorie = scanner.nextLine().trim();
            Categorie categorieCarte = Categorie.valueOf(categorie.toUpperCase());
            System.out.print("Num??r exemplare: ");
            int nrExemplare = scanner.nextInt();
            Carte carte = new Carte(titlu, autor, editura, anAparitie, nrPagini, categorieCarte);
            listaCarti.adaugaCarte(carte);
            registruCarti.adaugaCarte(carte.getIsbn(), nrExemplare);
            ColectieCarti colecieCarti = biblioteca.getColectii().get(categorieCarte);
            if (colecieCarti == null) {
                List<String> listaCarti = new ArrayList<>();
                listaCarti.add(carte.getIsbn());
                biblioteca.getColectii().put(categorieCarte, new ColectieCarti(listaCarti));
            } else {
                colecieCarti.adaugaCarte(carte.getIsbn());
            }
            System.out.println("Cartea a fost ad??ugat?? cu succes!");
            System.out.println(carte);
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Datele introduse nu sunt valide!" + lineSeparator);
            golesteBufferul();
        } catch (IllegalArgumentException e) {
            System.err.println("EROARE: Categorie invalid??!" + lineSeparator + "Categoriile valide sunt: "
                    + Arrays.toString(Categorie.values()) + lineSeparator);
        }
    }

    static void stergeCarteDinColectie() {
        try {
            golesteBufferul();
            System.out.print("V?? rug??m s?? introduce??i ISBN-ul c??r??ii pe care dori??i s?? o ??terge??i din colec??ie: ");
            String isbn = scanner.nextLine().trim();
            if (isbn.isEmpty() || isbn.isBlank() || isbn.length() != 17) {
                throw new ExceptieValidareISBN("ISBN-ul introdus nu este valid!");
            }
            Carte carte = listaCarti.cautaCarte(isbn);
            if (carte == null) {
                System.err.println("EROARE: Nu exist?? nicio carte cu acest ISBN!" + lineSeparator);
            } else {
                boolean esteImprumutata = listaImprumuturi.esteImprumutata(carte.getIsbn());
                if (esteImprumutata) {
                    System.err.println("EROARE: Nu se poate ??terge o carte care este ??mprumutat??!" + lineSeparator);
                } else {
                    biblioteca.getColectii().get(carte.getCategorie()).stergeCarte(isbn);
                    listaCarti.stergeCarte(carte);
                    registruCarti.stergeCarte(isbn);
                    System.out.println("Cartea a fost ??tears?? cu succes!");
                    System.out.println(carte);
                }
            }
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Datele introduse nu sunt valide!" + lineSeparator);
            golesteBufferul();
        } catch (ExceptieValidareISBN e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        }
    }

    static void genereazaRaportAutor() {
        try {
            golesteBufferul();
            System.out.print("V?? rug??m s?? introduce??i numele autorului: ");
            String numeAutor = scanner.nextLine().trim();
            if (numeAutor.isEmpty() || numeAutor.isBlank()) {
                throw new ExceptieValidareNumeAutor("Numele autorului nu este valid!");
            }
            List<Carte> cartiAutor = listaCarti.getCarti(numeAutor);
            if (cartiAutor.isEmpty()) {
                System.err.println("Nu exist?? nicio carte scris?? de acest autor!" + lineSeparator);
            } else {
                raport.genereazaRaportAutor(cartiAutor);
            }
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Datele introduse nu sunt valide!" + lineSeparator);
            golesteBufferul();
        } catch (ExceptieValidareNumeAutor e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        }
    }

    static void genereazaRaportColectie() {
        try {
            golesteBufferul();
            System.out.print("V?? rug??m s?? introduce??i numele colec??iei: ");
            String numeColectie = scanner.nextLine().trim();
            if (numeColectie.isEmpty() || numeColectie.isBlank()) {
                throw new ExceptieValidareNumeColectie("Numele colec??iei nu este valid!");
            }
            Categorie categorie = Categorie.valueOf(numeColectie.toUpperCase());
            ColectieCarti colectieCarti = biblioteca.getColectii().get(categorie);
            if (colectieCarti == null) {
                System.err.println("Nu exist?? nicio carte ??n aceast?? colec??ie!" + lineSeparator);
            } else {
                raport.genereazaRaportColectie(colectieCarti);
            }
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Datele introduse nu sunt valide!" + lineSeparator);
            golesteBufferul();
        } catch (IllegalArgumentException e) {
            System.err.println("EROARE: Categorie invalid??!" + lineSeparator + "Categoriile valide sunt: "
                    + Arrays.toString(Categorie.values()) + lineSeparator);
        } catch (ExceptieValidareNumeColectie e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        }
    }

    static void genereazaRaportData() {
        System.out.println("Genereaz?? raport privind c??r??ile ??mprumutate la o anumit?? dat??");
        try {
            golesteBufferul();
            System.out.print("V?? rug??m s?? introduce??i data (format: dd.MM.yyyy): ");
            String data = scanner.nextLine().trim();
            if (data.isEmpty() || data.isBlank()) {
                throw new ExceptieValidareData("Data nu este valid??!");
            }
            Date dataCautata = Data.convertesteData(data);
            List<Carte> cartiImprumutate = listaImprumuturi.getCartiImprumutate(dataCautata);
            if (cartiImprumutate.isEmpty()) {
                System.err.println("Nu exist?? nicio carte ??mprumutat?? la aceast?? dat??!" + lineSeparator);
            } else {
                raport.genereazaRaportDataImprumut(cartiImprumutate, dataCautata);
            }
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Datele introduse nu sunt valide!" + lineSeparator);
            golesteBufferul();
        } catch (DateTimeParseException e) {
            System.err.println("EROARE: Data nu este valid??!" + lineSeparator);
        } catch (ExceptieValidareData e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        }
    }

    static void inregistreazaCititor() {
        try {
            golesteBufferul();
            System.out.print("CNP: ");
            String cnp = scanner.nextLine();
            if (cnp.isEmpty() || cnp.isBlank() || cnp.length() != 13) {
                throw new ExceptieCNPInvalid("CNP invalid");
            }
            if (listaCititori.getCititor(cnp) != null) {
                throw new ExceptieCititorExistent("Cititorul exist?? deja");
            }
            System.out.print("Nume: ");
            String nume = scanner.nextLine();
            System.out.print("Prenume: ");
            String prenume = scanner.nextLine();
            System.out.print("Data na??terii (format: dd.MM.yyyy): ");
            Date dataNasterii = Data.convertesteData(scanner.nextLine());
            System.out.print("Adresa: ");
            String adresa = scanner.nextLine();
            System.out.print("Num??r de telefon: ");
            String nrTelefon = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Tip permis (ELEV, STUDENT): ");
            TipCititor tipCititor = TipCititor.valueOf(scanner.nextLine().toUpperCase());
            switch (tipCititor) {
                case ELEV -> cititor = new Elev(cnp, nume, prenume, dataNasterii, adresa, nrTelefon, email);
                case STUDENT -> cititor = new Student(cnp, nume, prenume, dataNasterii, adresa, nrTelefon, email);
            }
            listaCititori.adaugaCititor(cititor);
            imprumut = null;
            System.out.println("Cititorul a fost ??nregistrat cu succes!");
        } catch (ExceptieCNPInvalid | ExceptieCititorExistent e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        } catch (IllegalArgumentException e) {
            System.err.println("EROARE: Tipul cititorului nu este valid" + lineSeparator);
        }
    }

    static void imprumutaCarte() {
        golesteBufferul();
        try {
            if (cititor == null || cititor.getCnp() == null || cititor.getCnp().isEmpty()) {
                System.out.print("CNP: ");
                String cnp = scanner.nextLine();
                cititor = listaCititori.getCititor(cnp);
            }
            if (imprumut == null) {
                imprumut = new Imprumut(cititor.getCnp());
            }
        } catch (ExceptieCititorInexistent e) {
            System.err.println("EROARE: " + e.getMessage());
            System.err.println(
                    "V?? rog s?? v?? ??nregistra??i pentru a ob??ine un card de acces (permis de cititor)" + lineSeparator);
            return;
        }
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        if (isbn.isEmpty() || isbn.isBlank() || isbn.length() != 17) {
            System.err.println("EROARE: ISBN-ul nu este valid" + lineSeparator);
            return;
        }
        if (listaCarti.cautaCarte(isbn) == null) {
            System.err.println("EROARE: Cartea nu exist?? ??n bibliotec??" + lineSeparator);
            return;
        }
        if (!registruCarti.verificaDisponibilitate(isbn)) {
            System.err.println("EROARE: Cartea nu este disponibil??" + lineSeparator);
            return;
        }
        try {
            imprumut.adaugaCarte(isbn);
            registruCarti.imprumutaCarte(isbn);
            System.out.println("Cartea a fost ??mprumutat?? cu succes!");
            Carte carte = listaCarti.cautaCarte(isbn);
            System.out.println(carte.toString());
            System.out.println("Data ??mprumutului: " + Data.convertesteData(imprumut.getDataImprumut()));
            System.out.println("Data return??rii: " + Data.convertesteData(imprumut.getDataRetur().get(isbn)));
        } catch (UnsupportedOperationException e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
        }
    }

    static void returneazaCarte() {
        golesteBufferul();
        try {
            if (cititor == null || cititor.getCnp() == null || cititor.getCnp().isEmpty()) {
                System.out.print("CNP: ");
                String cnp = scanner.nextLine();
                cititor = listaCititori.getCititor(cnp);
                if (cititor == null) {
                    throw new ExceptieCititorInexistent("Cititorul nu exist??");
                }
            }
            if (imprumut == null) {
                imprumut = Arrays.stream(listaImprumuturi.getListaImprumuturi())
                        .filter(i -> i.getCnpCititor().equals(cititor.getCnp())).findFirst().orElse(null);
            }
            if (imprumut == null) {
                throw new ExceptieImprumutInexistent("Nu exist?? niciun ??mprumut ??nregistrat pentru cititorul "
                        + cititor.getNume() + " " + cititor.getPrenume());
            }
            listaImprumuturi.stergeImprumut(imprumut);
        } catch (ExceptieImprumutInexistent | ExceptieCititorInexistent e) {
            System.err.println("EROARE: " + e.getMessage() + lineSeparator);
            return;
        }
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        if (isbn.isEmpty() || isbn.isBlank() || isbn.length() != 17) {
            System.err.println("EROARE: ISBN-ul nu este valid" + lineSeparator);
            return;
        }
        Carte carte = listaCarti.cautaCarte(isbn);
        if (carte == null) {
            System.err.println("EROARE: Cartea nu exist?? ??n bibliotec??" + lineSeparator);
            return;
        }
        List<?> cartiImprumutate = imprumut.getCartiImprumutate();
        if (!cartiImprumutate.contains(isbn)) {
            System.err.println("EROARE: Cartea nu a fost ??mprumutat?? de cititorul " + cititor.getNume() + " "
                    + cititor.getPrenume() + lineSeparator);
            return;
        }
        Date today = new Date();
        Date dataRetur = imprumut.getDataRetur().get(isbn);
        if (today.after(dataRetur)) {
            System.err.println("EROARE: Cartea a fost returnat?? ??ntr-o dat?? ulterioar?? datei de returnare");
            System.err.println("Data return??rii: " + Data.convertesteData(dataRetur));
            System.err.println("Data return??rii efective: " + Data.convertesteData(today));
            System.err.println(
                    "V?? rug??m s?? pl??ti??i o amend?? de " + imprumut.calculeazaAmenzi(isbn) + " lei" + lineSeparator);
            return;
        }
        imprumut.stergeCarte(isbn);
        registruCarti.returneazaCarte(isbn);
        System.out.println(
                "Cartea " + carte.getTitlu() + ", scris?? de " + carte.getAutor() + " a fost returnat?? cu succes!");
    }

    static void iesire() {
        if (imprumut != null && !imprumut.getCartiImprumutate().isEmpty()) {
            listaImprumuturi.adaugaImprumut(imprumut);
        }
        salveazaDate();
        System.exit(0);
    }

    static void optiuneInvalida() {
        System.out.println("Op??iune invalid??");
    }

    static void golesteBufferul() {
        scanner.nextLine();
    }

    static void citesteOptiunea() {
        try {
            System.out.print("> ");
            optiune = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("EROARE: Op??iunea trebuie s?? fie un num??r ??ntreg!");
            System.exit(0);
        }
    }
}
