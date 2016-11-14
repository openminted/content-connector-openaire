package eu.openminted.openaire;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "class")
@XmlEnum
public enum ResultRelationsEnum {
    /**
     * Project has contact a person; dnet:project_person_relations
     *
     */
    @XmlEnumValue("hasContact")
    HAS_CONTACT("hasContact"),
    /**
     * Person is contact for a project; dnet:project_person_relations
     *
     */
    @XmlEnumValue("isContact")
    IS_CONTACT("isContact"),
    /**
     * Project produces a result; dnet:project_result_relations
     *
     */
    @XmlEnumValue("produces")
    PRODUCES("produces"),
    /**
     * Result is produced by project; dnet:project_result_relations
     *
     */
    @XmlEnumValue("isProducedBy")
    IS_PRODUCED_BY("isProducedBy"),

    @XmlEnumValue("hasParticipant")
    HAS_PARTICIPANT("hasParticipant"),

    @XmlEnumValue("isParticipant")
    IS_PARTICIPANT("isParticipant"),
    /**
     * Document has author; dnet:person_result_relations
     *
     */
    @XmlEnumValue("hasAuthor")
    HAS_AUTHOR("hasAuthor"),
    /**
     * Is author of document; dnet:person_result_relations
     *
     */
    @XmlEnumValue("isAuthorOf")
    IS_AUTHOR_OF("isAuthorOf"),

    @XmlEnumValue("isCoauthorOf")
    IS_COAUTHOR_OF("isCoauthorOf"),
    /**
     * Document is among top N similar documents; dnet:result_result_relations
     *
     */
    @XmlEnumValue("isAmongTopNSimilarDocuments")
    IS_AMONG_TOP_N_SIMILAR_DOCUMENTS("isAmongTopNSimilarDocuments"),
    /**
     * Document has N other similar documents; dnet:result_result_relations
     *
     */
    @XmlEnumValue("hasAmongTopNSimilarDocuments")
    HAS_AMONG_TOP_N_SIMILAR_DOCUMENTS("hasAmongTopNSimilarDocuments"),
    /**
     * Document is related to other documents; dnet:result_result_relations
     *
     */
    @XmlEnumValue("isRelatedTo")
    IS_RELATED_TO("isRelatedTo"),
    @XmlEnumValue("isProducedBy")
    IS_PROVIDED_BY("isProvidedBy"),

    @XmlEnumValue("provides")
    PROVIDEDS("provides"),

    @XmlEnumValue("other")
    OTHER("other");
    private final String value;

    ResultRelationsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResultRelationsEnum fromValue(String v) {
        for (ResultRelationsEnum c: ResultRelationsEnum.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

/*
Information about the related entity. <p>The semantics of the relationships is expressed by the attributes class and scheme. </p>
<!--  <p>See the following ontology for the available relationships: $ontologyURL$</p>-->
<p>Allowed relationships are: <table>
        <tr><th>Entity types</th><th>Name of relationships</th><th>Inverse of</th><th>Simmetric</th></tr>
        <tr><td>Project -- Person</td><td>hasContact</td><td>isContact</td><td>no</td></tr>
        <tr><td>Project -- Result</td><td>produces</td><td>isProducedBy</td><td>no</td></tr>
        <tr><td>Project -- Organization</td><td>hasParticipant</td><td>isParticipant</td><td>no</td></tr>
        <tr><td>Person -- Project</td><td>isContact</td><td>hasContact</td><td>no</td></tr>
        <tr><td>Person -- Result</td><td>isAuthorOf</td><td>hasAuthor</td><td>no</td></tr>
        <tr><td>Person -- Person</td><td>isCoauthorOf</td><td>--</td><td>yes</td></tr>
        <tr><td>Result -- Person</td><td>hasAuthor</td><td>isAuthorOf</td><td>no</td></tr>
        <tr><td>Result -- Project</td><td>isProducedBy</td><td>produces</td><td>no</td></tr>
        <tr><td>Result -- Result</td><td>isRelatedTo</td><td>--</td><td>yes</td></tr>
        <tr><td>Result -- Result</td><td>hasAmongTopNSimilarDocuments</td><td>isAmongTopNSimilarDocuments</td><td>no</td></tr>
        <tr><td>Result -- Result</td><td>isAmongTopNSimilarDocuments</td><td>hasAmongTopNSimilarDocuments</td><td>no</td></tr>
        <tr><td>Organization -- Datasource</td><td>isProvidedBy</td><td>provides</td><td>no</td></tr>
        <tr><td>Organization -- Project</td><td>isParticpant</td><td>hasParticipant</td><td>no</td></tr>
        <tr><td>Datasource -- Organization</td><td>provides</td><td>isProvidedBy</td><td>no</td></tr>
    </table></p>
 */