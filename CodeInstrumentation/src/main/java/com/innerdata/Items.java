package com.innerdata;

import com.mainpackage.InstrumentationClass;
import com.xmlparsing.Location;

import java.util.ArrayList;
import java.util.List;

public class Items {

    private InstrumentationClass.Items type;
    private List<Locations> locations;

    Items(InstrumentationClass.Items type, List<Location> argLocations) {
        this.type = type;
        this.locations = new ArrayList<>();
        argLocations.forEach(location -> {
            switch (location.getPlace()){
                case "before":
                    locations.add(new Locations(InstrumentationClass.Locations.BEFORE, location.getMessage()));
                    break;
                case "after":
                    locations.add(new Locations(InstrumentationClass.Locations.AFTER, location.getMessage()));
                    break;
                case "return":
                    locations.add(new Locations(InstrumentationClass.Locations.AFTER_RETURN, location.getMessage()));
                    break;
                case "exception":
                    locations.add(new Locations(InstrumentationClass.Locations.AFTER_THROWING, location.getMessage()));
                    break;
                default:
                    System.out.println(location.getPlace() + "location is unacceptable for instrumentation");
                    System.exit(3);
            }
        });
    }

    public List<Locations> getLocation() {
        return locations;
    }

    public InstrumentationClass.Items getType() {
        return type;
    }

}
