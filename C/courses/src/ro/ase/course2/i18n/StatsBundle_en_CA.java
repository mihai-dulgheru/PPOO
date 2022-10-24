package ro.ase.course2.i18n;

import java.util.ListResourceBundle;

public class StatsBundle_en_CA extends ListResourceBundle {

    private Object[][] contents = {
            {"GDP", new Integer(21300)},
            {"Population", new Integer(125449703)},
            {"Literacy", new Double(0.99)},
    };

    public Object[][] getContents() {
        return contents;
    }
}
