package InnerData;

import MainPackage.InstrumentationClass;
import XMLParsing.Item;

import java.util.ArrayList;
import java.util.List;

public class InstrumentationData {

    private List<Items> items;

    public InstrumentationData(List<Item> argItems) {
        this.items = new ArrayList<>();
        argItems.forEach(item -> {
            switch (item.getType()){
                case "if":
                    items.add(new Items(InstrumentationClass.Items.IF, item.getLocation()));
                    break;
                case "then":
                    items.add(new Items(InstrumentationClass.Items.THEN, item.getLocation()));
                    break;
                case "else":
                    items.add(new Items(InstrumentationClass.Items.ELSE, item.getLocation()));
                    break;
                case "switch":
                    items.add(new Items(InstrumentationClass.Items.SWITCH, item.getLocation()));
                    break;
                case "case":
                    items.add(new Items(InstrumentationClass.Items.CASE, item.getLocation()));
                    break;
                case "while":
                    items.add(new Items(InstrumentationClass.Items.WHILE, item.getLocation()));
                    break;
                case "do":
                    items.add(new Items(InstrumentationClass.Items.DO, item.getLocation()));
                    break;
                case "for":
                    items.add(new Items(InstrumentationClass.Items.FOR, item.getLocation()));
                    break;
                case "foreach":
                    items.add(new Items(InstrumentationClass.Items.FOREACH, item.getLocation()));
                    break;
                case "*":
                    items.add(new Items(InstrumentationClass.Items.ALL, item.getLocation()));
                    break;
                default:
                    System.err.println(item.getType() + "%type is unacceptable for instrumentation");
                    System.exit(2);
            }
        });
    }

    public List<Items> getItems() {
        return items;
    }

}
