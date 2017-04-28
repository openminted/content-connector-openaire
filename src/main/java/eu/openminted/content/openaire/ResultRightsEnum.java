package eu.openminted.content.openaire;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "class")
@XmlEnum
public enum ResultRightsEnum {

    @XmlEnumValue("12MONTHS")
    TWELVE_MONTHS("12 Months Embargo", "12MONTHS"),

    @XmlEnumValue("6 Months Embargo")
    SIX_MONTHS("6 Months Embargo", "6MONTHS"),

    @XmlEnumValue("Closed Access")
    CLOSED("Closed Access", "CLOSED"),

    @XmlEnumValue("Embargo")
    EMBARGO("Embargo", "EMBARGO"),

    @XmlEnumValue("Open Access")
    OPEN("Open Access", "OPEN"),

    @XmlEnumValue("Other")
    OTHER("Other", "OTHER"),

    @XmlEnumValue("Restricted")
    RESTRICTED("Restricted", "RESTRICTED"),

    /**
     * `Unknown` is not a accurate OpenAire Access vocabulary.<br/>
     * `Not available` should be used instead but for shake
     * of simplicity and abstraction we use `Unknown`.
     */
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown", "UNKNOWN");

    private final String value;
    private final String id;

    ResultRightsEnum(String v, String id) {
        this.value = v;
        this.id = id;
    }

    public String value() {
        return value;
    }

    public String id() {
        return id;
    }

    public static ResultRightsEnum fromValue(String v) {
        for (ResultRightsEnum c : ResultRightsEnum.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static ResultRightsEnum fromId(String v) {
        for (ResultRightsEnum c : ResultRightsEnum.values()) {
            if (c.id.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}