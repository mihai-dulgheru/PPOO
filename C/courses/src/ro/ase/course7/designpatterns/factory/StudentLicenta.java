package ro.ase.course7.designpatterns.factory;

public class StudentLicenta extends Student {
    public StudentLicenta(String nume, float medie) {
        this.nume = nume;
        this.medie = medie;
    }

    @Override
    public void afisareStudent() {
        System.out.println("Afisare student licenta");
    }
}
