package gluePaP.semantics;

public class SemAtom extends Identifier {
    private String name;
    private String value;
    //private Type type;



    public SemAtom(String name, String value)
    {
        this.name = name;
        this.value = value;
    }





    //Getter and Setter methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
