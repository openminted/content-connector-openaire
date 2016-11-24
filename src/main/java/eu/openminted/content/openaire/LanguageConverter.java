package eu.openminted.content.openaire;

import java.util.HashMap;
import java.util.Map;

public class LanguageConverter {

    private static LanguageConverter languageConverter;

    public static LanguageConverter getInstance() {
        if (languageConverter == null) {
            languageConverter = new LanguageConverter();
        }
        return languageConverter;
    }

    /*
       List of language names and codes obtained from pycountry.
       To build this file use the following command:
       python misc/tools/lang/gen_langs.py > clarinel/media/admin/js/pycountry.js
    */
    private String[] _lang_code_list = {
            "aa", "ab", "ace", "ach", "ada", "ady", "afa", "afh", "af", "ain",
            "ak", "akk", "sq", "ale", "alg", "alt", "am", "ang", "anp", "apa",
            "ar", "arc", "an", "hy", "arn", "arp", "art", "arw", "as", "ast",
            "ath", "aus", "av", "ae", "awa", "ay", "az", "bad", "bai", "ba",
            "bal", "bm", "ban", "eu", "bas", "bat", "bej", "be", "bem", "bn",
            "ber", "bho", "bh", "bik", "bin", "bi", "bla", "bnt", "bs", "bra",
            "br", "btk", "bua", "bug", "bg", "my", "byn", "cad", "cai", "car",
            "ca", "cau", "ceb", "cel", "ch", "chb", "ce", "chg", "zh", "chk",
            "chm", "chn", "cho", "chp", "chr", "cu", "cv", "chy", "cmc", "cop",
            "kw", "co", "cpe", "cpf", "cpp", "cr", "crh", "crp", "csb", "cus",
            "cs", "dak", "da", "dar", "day", "del", "den", "dgr", "din", "dv",
            "doi", "dra", "dsb", "dua", "dum", "nl", "dyu", "dz", "efi", "egy",
            "eka", "elx", "en", "enm", "eo", "et", "ee", "ewo", "fan", "fo",
            "fat", "fj", "fil", "fi", "fiu", "fon", "fr", "frm", "fro", "frr",
            "frs", "fy", "ff", "fur", "gaa", "gay", "gba", "gem", "ka", "de",
            "gez", "gil", "gd", "ga", "gl", "gv", "gmh", "goh", "gon", "gor",
            "got", "grb", "grc", "el", "gn", "gsw", "gu", "gwi", "hai", "ht",
            "ha", "haw", "he", "hz", "hil", "him", "hi", "hit", "hmn", "ho",
            "hr", "hsb", "hu", "hup", "iba", "ig", "is", "io", "ii", "ijo",
            "iu", "ie", "ilo", "ia", "inc", "id", "ine", "inh", "ik", "ira",
            "iro", "it", "jv", "jbo", "ja", "jpr", "jrb", "kaa", "kab", "kac",
            "kl", "kam", "kn", "kar", "ks", "kr", "kaw", "kk", "kbd", "kha",
            "khi", "km", "kho", "ki", "rw", "ky", "kmb", "kok", "kv", "kg",
            "ko", "kos", "kpe", "krc", "krl", "kro", "kru", "kj", "kum", "ku",
            "kut", "lad", "lah", "lam", "lo", "la", "lv", "lez", "li", "ln",
            "lt", "lol", "loz", "lb", "lua", "lu", "lg", "lui", "lun", "luo",
            "lus", "mk", "mad", "mag", "mh", "mai", "mak", "ml", "man", "mi",
            "map", "mr", "mas", "ms", "mdf", "mdr", "men", "mga", "mic", "min",
            "mis", "mkh", "mg", "mt", "mnc", "mni", "mno", "moh", "mo", "mn",
            "mos", "mul", "mun", "mus", "mwl", "mwr", "myn", "myv", "nah", "nai",
            "nap", "na", "nv", "nr", "nd", "ng", "nds", "ne", "new", "nia",
            "nic", "niu", "nn", "nb", "nog", "non", "no", "nqo", "nso", "nub",
            "nwc", "ny", "nym", "nyn", "nyo", "nzi", "oc", "oj", "or", "om",
            "osa", "os", "ota", "oto", "paa", "pag", "pal", "pam", "pa", "pap",
            "pau", "peo", "fa", "phi", "phn", "pi", "pl", "pon", "pt", "pra",
            "pro", "ps", "qu", "raj", "rap", "rar", "roa", "rm", "rom", "ro",
            "rn", "rup", "ru", "sad", "sg", "sah", "sai", "sal", "sam", "sa",
            "sas", "sat", "scn", "sco", "sel", "sem", "sga", "sgn", "shn", "sid",
            "si", "sio", "sit", "sla", "sk", "sl", "sma", "se", "smi", "smj",
            "smn", "sm", "sms", "sn", "sd", "snk", "sog", "so", "son", "st",
            "es", "sc", "srn", "sr", "srr", "ssa", "ss", "suk", "su", "sus",
            "sux", "sw", "sv", "syc", "syr", "ty", "tai", "ta", "tt", "te",
            "tem", "ter", "tet", "tg", "tl", "th", "bo", "tig", "ti", "tiv",
            "tkl", "tlh", "tli", "tmh", "tog", "to", "tpi", "tsi", "tn", "ts",
            "tk", "tum", "tup", "tr", "tut", "tvl", "tw", "tyv", "udm", "uga",
            "ug", "uk", "umb", "und", "ur", "uz", "vai", "ve", "vi", "vo",
            "vot", "wak", "wal", "war", "was", "cy", "wen", "wa", "wo", "xal",
            "xh", "yao", "yap", "yi", "yo", "ypk", "zap", "zbl", "zen", "za",
            "znd", "zu", "zun", "zza"};

    private String[] _lang_name_list = {
            "Afar", "Abkhazian", "Achinese", "Acoli",
            "Adangme", "Adyghe; Adygei", "Afro-Asiatic languages", "Afrihili",
            "Afrikaans", "Ainu", "Akan", "Akkadian",
            "Albanian", "Aleut", "Algonquian languages", "Southern Altai",
            "Amharic", "English, Old (ca. 450-1100)", "Angika", "Apache languages",
            "Arabic", "Official Aramaic (700-300 BCE); Imperial Aramaic (700-300 BCE)", "Aragonese", "Armenian",
            "Mapudungun; Mapuche", "Arapaho", "Artificial languages", "Arawak",
            "Assamese", "Asturian; Bable; Leonese; Asturleonese", "Athapascan languages", "Australian languages",
            "Avaric", "Avestan", "Awadhi", "Aymara",
            "Azerbaijani", "Banda languages", "Bamileke languages", "Bashkir",
            "Baluchi", "Bambara", "Balinese", "Basque",
            "Basa", "Baltic languages", "Beja; Bedawiyet", "Belarusian",
            "Bemba", "Bengali", "Berber languages", "Bhojpuri",
            "Bihari languages", "Bikol", "Bini; Edo", "Bislama",
            "Siksika", "Bantu languages", "Bosnian", "Braj",
            "Breton", "Batak languages", "Buriat", "Buginese",
            "Bulgarian", "Burmese", "Blin; Bilin", "Caddo",
            "Central American Indian languages", "Galibi Carib", "Catalan; Valencian", "Caucasian languages",
            "Cebuano", "Celtic languages", "Chamorro", "Chibcha",
            "Chechen", "Chagatai", "Chinese", "Chuukese",
            "Mari", "Chinook jargon", "Choctaw", "Chipewyan; Dene Suline",
            "Cherokee", "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic", "Chuvash", "Cheyenne",
            "Chamic languages", "Coptic", "Cornish", "Corsican",
            "Creoles and pidgins, English based", "Creoles and pidgins, French-based", "Creoles and pidgins, Portuguese-based", "Cree",
            "Crimean Tatar; Crimean Turkish", "Creoles and pidgins", "Kashubian", "Cushitic languages",
            "Czech", "Dakota", "Danish", "Dargwa",
            "Land Dayak languages", "Delaware", "Slave (Athapascan)", "Dogrib",
            "Dinka", "Divehi; Dhivehi; Maldivian", "Dogri", "Dravidian languages",
            "Lower Sorbian", "Duala", "Dutch, Middle (ca. 1050-1350)", "Dutch; Flemish",
            "Dyula", "Dzongkha", "Efik", "Egyptian (Ancient)",
            "Ekajuk", "Elamite", "English", "English, Middle (1100-1500)",
            "Esperanto", "Estonian", "Ewe", "Ewondo",
            "Fang", "Faroese", "Fanti", "Fijian",
            "Filipino; Pilipino", "Finnish", "Finno-Ugrian languages", "Fon",
            "French", "French, Middle (ca. 1400-1600)", "French, Old (842-ca. 1400)", "Northern Frisian",
            "Eastern Frisian", "Western Frisian", "Fulah", "Friulian",
            "Ga", "Gayo", "Gbaya", "Germanic languages",
            "Georgian", "German", "Geez", "Gilbertese",
            "Gaelic; Scottish Gaelic", "Irish", "Galician", "Manx",
            "German, Middle High (ca. 1050-1500)", "German, Old High (ca. 750-1050)", "Gondi", "Gorontalo",
            "Gothic", "Grebo", "Greek, Ancient (to 1453)", "Greek, Modern (1453-)",
            "Guarani", "Swiss German; Alemannic; Alsatian", "Gujarati", "Gwich'in",
            "Haida", "Haitian; Haitian Creole", "Hausa", "Hawaiian",
            "Hebrew", "Herero", "Hiligaynon", "Himachali languages; Western Pahari languages",
            "Hindi", "Hittite", "Hmong; Mong", "Hiri Motu",
            "Croatian", "Upper Sorbian", "Hungarian", "Hupa",
            "Iban", "Igbo", "Icelandic", "Ido",
            "Sichuan Yi; Nuosu", "Ijo languages", "Inuktitut", "Interlingue; Occidental",
            "Iloko", "Interlingua (International Auxiliary Language Association)", "Indic languages", "Indonesian",
            "Indo-European languages", "Ingush", "Inupiaq", "Iranian languages",
            "Iroquoian languages", "Italian", "Javanese", "Lojban",
            "Japanese", "Judeo-Persian", "Judeo-Arabic", "Kara-Kalpak",
            "Kabyle", "Kachin; Jingpho", "Kalaallisut; Greenlandic", "Kamba",
            "Kannada", "Karen languages", "Kashmiri", "Kanuri",
            "Kawi", "Kazakh", "Kabardian", "Khasi",
            "Khoisan languages", "Central Khmer", "Khotanese;Sakan", "Kikuyu; Gikuyu",
            "Kinyarwanda", "Kirghiz; Kyrgyz", "Kimbundu", "Konkani",
            "Komi", "Kongo", "Korean", "Kosraean",
            "Kpelle", "Karachay-Balkar", "Karelian", "Kru languages",
            "Kurukh", "Kuanyama; Kwanyama", "Kumyk", "Kurdish",
            "Kutenai", "Ladino", "Lahnda", "Lamba",
            "Lao", "Latin", "Latvian", "Lezghian",
            "Limburgan; Limburger; Limburgish", "Lingala", "Lithuanian", "Mongo",
            "Lozi", "Luxembourgish; Letzeburgesch", "Luba-Lulua", "Luba-Katanga",
            "Ganda", "Luiseno", "Lunda", "Luo (Kenya and Tanzania)",
            "Lushai", "Macedonian", "Madurese", "Magahi",
            "Marshallese", "Maithili", "Makasar", "Malayalam",
            "Mandingo", "Maori", "Austronesian languages", "Marathi",
            "Masai", "Malay", "Moksha", "Mandar",
            "Mende", "Irish, Middle (900-1200)", "Mi'kmaq; Micmac", "Minangkabau",
            "Uncoded languages", "Mon-Khmer languages", "Malagasy", "Maltese",
            "Manchu", "Manipuri", "Manobo languages", "Mohawk",
            "Moldavian; Moldovan", "Mongolian", "Mossi", "Multiple languages",
            "Munda languages", "Creek", "Mirandese", "Marwari",
            "Mayan languages", "Erzya", "Nahuatl languages", "North American Indian languages",
            "Neapolitan", "Nauru", "Navajo; Navaho", "Ndebele, South; South Ndebele",
            "Ndebele, North; North Ndebele", "Ndonga", "Low German; Low Saxon; German, Low; Saxon, Low", "Nepali",
            "Nepal Bhasa; Newari", "Nias", "Niger-Kordofanian languages", "Niuean",
            "Norwegian Nynorsk; Nynorsk, Norwegian", "BokmC%l, Norwegian; Norwegian BokmC%l", "Nogai", "Norse, Old",
            "Norwegian", "N'Ko", "Pedi; Sepedi; Northern Sotho", "Nubian languages",
            "Classical Newari; Old Newari; Classical Nepal Bhasa", "Chichewa; Chewa; Nyanja", "Nyamwezi", "Nyankole",
            "Nyoro", "Nzima", "Occitan (post 1500)", "Ojibwa",
            "Oriya", "Oromo", "Osage", "Ossetian; Ossetic",
            "Turkish, Ottoman (1500-1928)", "Otomian languages", "Papuan languages", "Pangasinan",
            "Pahlavi", "Pampanga; Kapampangan", "Panjabi; Punjabi", "Papiamento",
            "Palauan", "Persian, Old (ca. 600-400 B.C.)", "Persian", "Philippine languages",
            "Phoenician", "Pali", "Polish", "Pohnpeian",
            "Portuguese", "Prakrit languages", "ProvenC'al, Old (to 1500); Occitan, Old (to 1500)", "Pushto; Pashto",
            "Quechua", "Rajasthani", "Rapanui", "Rarotongan; Cook Islands Maori",
            "Romance languages", "Romansh", "Romany", "Romanian",
            "Rundi", "Aromanian; Arumanian; Macedo-Romanian", "Russian", "Sandawe",
            "Sango", "Yakut", "South American Indian languages", "Salishan languages",
            "Samaritan Aramaic", "Sanskrit", "Sasak", "Santali",
            "Sicilian", "Scots", "Selkup", "Semitic languages",
            "Irish, Old (to 900)", "Sign Languages", "Shan", "Sidamo",
            "Sinhala; Sinhalese", "Siouan languages", "Sino-Tibetan languages", "Slavic languages",
            "Slovak", "Slovenian", "Southern Sami", "Northern Sami",
            "Sami languages", "Lule Sami", "Inari Sami", "Samoan",
            "Skolt Sami", "Shona", "Sindhi", "Soninke",
            "Sogdian", "Somali", "Songhai languages", "Sotho, Southern",
            "Spanish; Castilian", "Sardinian", "Sranan Tongo", "Serbian",
            "Serer", "Nilo-Saharan languages", "Swati", "Sukuma",
            "Sundanese", "Susu", "Sumerian", "Swahili",
            "Swedish", "Classical Syriac", "Syriac", "Tahitian",
            "Tai languages", "Tamil", "Tatar", "Telugu",
            "Timne", "Tereno", "Tetum", "Tajik",
            "Tagalog", "Thai", "Tibetan", "Tigre",
            "Tigrinya", "Tiv", "Tokelau", "Klingon; tlhIngan-Hol",
            "Tlingit", "Tamashek", "Tonga (Nyasa)", "Tonga (Tonga Islands)",
            "Tok Pisin", "Tsimshian", "Tswana", "Tsonga",
            "Turkmen", "Tumbuka", "Tupi languages", "Turkish",
            "Altaic languages", "Tuvalu", "Twi", "Tuvinian",
            "Udmurt", "Ugaritic", "Uighur; Uyghur", "Ukrainian",
            "Umbundu", "Undetermined", "Urdu", "Uzbek",
            "Vai", "Venda", "Vietnamese", "VolapC<k",
            "Votic", "Wakashan languages", "Wolaitta; Wolaytta", "Waray",
            "Washo", "Welsh", "Sorbian languages", "Walloon",
            "Wolof", "Kalmyk; Oirat", "Xhosa", "Yao",
            "Yapese", "Yiddish", "Yoruba", "Yupik languages",
            "Zapotec", "Blissymbols; Blissymbolics; Bliss", "Zenaga", "Zhuang; Chuang",
            "Zande languages", "Zulu", "Zuni", "Zaza; Dimili; Dimli; Kirdki; Kirmanjki; Zazaki"};


    private Map<String, String> _lang_code_to_name = new HashMap<>();
    private Map<String, String> _lang_name_to_code = new HashMap<>();

    private LanguageConverter() {
        _lang_code_to_name.put("aa", "Afar");
        _lang_code_to_name.put("ab", "Abkhazian");
        _lang_code_to_name.put("ace", "Achinese");
        _lang_code_to_name.put("ach", "Acoli");
        _lang_code_to_name.put("ada", "Adangme");
        _lang_code_to_name.put("ady", "Adyghe; Adygei");
        _lang_code_to_name.put("afa", "Afro-Asiatic languages");
        _lang_code_to_name.put("afh", "Afrihili");
        _lang_code_to_name.put("af", "Afrikaans");
        _lang_code_to_name.put("ain", "Ainu");
        _lang_code_to_name.put("ak", "Akan");
        _lang_code_to_name.put("akk", "Akkadian");
        _lang_code_to_name.put("sq", "Albanian");
        _lang_code_to_name.put("ale", "Aleut");
        _lang_code_to_name.put("alg", "Algonquian languages");
        _lang_code_to_name.put("alt", "Southern Altai");
        _lang_code_to_name.put("am", "Amharic");
        _lang_code_to_name.put("ang", "English, Old (ca. 450-1100)");
        _lang_code_to_name.put("anp", "Angika");
        _lang_code_to_name.put("apa", "Apache languages");
        _lang_code_to_name.put("ar", "Arabic");
        _lang_code_to_name.put("arc", "Official Aramaic (700-300 BCE); Imperial Aramaic (700-300 BCE)");
        _lang_code_to_name.put("an", "Aragonese");
        _lang_code_to_name.put("hy", "Armenian");
        _lang_code_to_name.put("arn", "Mapudungun; Mapuche");
        _lang_code_to_name.put("arp", "Arapaho");
        _lang_code_to_name.put("art", "Artificial languages");
        _lang_code_to_name.put("arw", "Arawak");
        _lang_code_to_name.put("as", "Assamese");
        _lang_code_to_name.put("ast", "Asturian; Bable; Leonese; Asturleonese");
        _lang_code_to_name.put("ath", "Athapascan languages");
        _lang_code_to_name.put("aus", "Australian languages");
        _lang_code_to_name.put("av", "Avaric");
        _lang_code_to_name.put("ae", "Avestan");
        _lang_code_to_name.put("awa", "Awadhi");
        _lang_code_to_name.put("ay", "Aymara");
        _lang_code_to_name.put("az", "Azerbaijani");
        _lang_code_to_name.put("bad", "Banda languages");
        _lang_code_to_name.put("bai", "Bamileke languages");
        _lang_code_to_name.put("ba", "Bashkir");
        _lang_code_to_name.put("bal", "Baluchi");
        _lang_code_to_name.put("bm", "Bambara");
        _lang_code_to_name.put("ban", "Balinese");
        _lang_code_to_name.put("eu", "Basque");
        _lang_code_to_name.put("bas", "Basa");
        _lang_code_to_name.put("bat", "Baltic languages");
        _lang_code_to_name.put("bej", "Beja; Bedawiyet");
        _lang_code_to_name.put("be", "Belarusian");
        _lang_code_to_name.put("bem", "Bemba");
        _lang_code_to_name.put("bn", "Bengali");
        _lang_code_to_name.put("ber", "Berber languages");
        _lang_code_to_name.put("bho", "Bhojpuri");
        _lang_code_to_name.put("bh", "Bihari languages");
        _lang_code_to_name.put("bik", "Bikol");
        _lang_code_to_name.put("bin", "Bini; Edo");
        _lang_code_to_name.put("bi", "Bislama");
        _lang_code_to_name.put("bla", "Siksika");
        _lang_code_to_name.put("bnt", "Bantu languages");
        _lang_code_to_name.put("bs", "Bosnian");
        _lang_code_to_name.put("bra", "Braj");
        _lang_code_to_name.put("br", "Breton");
        _lang_code_to_name.put("btk", "Batak languages");
        _lang_code_to_name.put("bua", "Buriat");
        _lang_code_to_name.put("bug", "Buginese");
        _lang_code_to_name.put("bg", "Bulgarian");
        _lang_code_to_name.put("my", "Burmese");
        _lang_code_to_name.put("byn", "Blin; Bilin");
        _lang_code_to_name.put("cad", "Caddo");
        _lang_code_to_name.put("cai", "Central American Indian languages");
        _lang_code_to_name.put("car", "Galibi Carib");
        _lang_code_to_name.put("ca", "Catalan; Valencian");
        _lang_code_to_name.put("cau", "Caucasian languages");
        _lang_code_to_name.put("ceb", "Cebuano");
        _lang_code_to_name.put("cel", "Celtic languages");
        _lang_code_to_name.put("ch", "Chamorro");
        _lang_code_to_name.put("chb", "Chibcha");
        _lang_code_to_name.put("ce", "Chechen");
        _lang_code_to_name.put("chg", "Chagatai");
        _lang_code_to_name.put("zh", "Chinese");
        _lang_code_to_name.put("chk", "Chuukese");
        _lang_code_to_name.put("chm", "Mari");
        _lang_code_to_name.put("chn", "Chinook jargon");
        _lang_code_to_name.put("cho", "Choctaw");
        _lang_code_to_name.put("chp", "Chipewyan; Dene Suline");
        _lang_code_to_name.put("chr", "Cherokee");
        _lang_code_to_name.put("cu", "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic");
        _lang_code_to_name.put("cv", "Chuvash");
        _lang_code_to_name.put("chy", "Cheyenne");
        _lang_code_to_name.put("cmc", "Chamic languages");
        _lang_code_to_name.put("cop", "Coptic");
        _lang_code_to_name.put("kw", "Cornish");
        _lang_code_to_name.put("co", "Corsican");
        _lang_code_to_name.put("cpe", "Creoles and pidgins, English based");
        _lang_code_to_name.put("cpf", "Creoles and pidgins, French-based");
        _lang_code_to_name.put("cpp", "Creoles and pidgins, Portuguese-based");
        _lang_code_to_name.put("cr", "Cree");
        _lang_code_to_name.put("crh", "Crimean Tatar; Crimean Turkish");
        _lang_code_to_name.put("crp", "Creoles and pidgins");
        _lang_code_to_name.put("csb", "Kashubian");
        _lang_code_to_name.put("cus", "Cushitic languages");
        _lang_code_to_name.put("cs", "Czech");
        _lang_code_to_name.put("dak", "Dakota");
        _lang_code_to_name.put("da", "Danish");
        _lang_code_to_name.put("dar", "Dargwa");
        _lang_code_to_name.put("day", "Land Dayak languages");
        _lang_code_to_name.put("del", "Delaware");
        _lang_code_to_name.put("den", "Slave (Athapascan)");
        _lang_code_to_name.put("dgr", "Dogrib");
        _lang_code_to_name.put("din", "Dinka");
        _lang_code_to_name.put("dv", "Divehi; Dhivehi; Maldivian");
        _lang_code_to_name.put("doi", "Dogri");
        _lang_code_to_name.put("dra", "Dravidian languages");
        _lang_code_to_name.put("dsb", "Lower Sorbian");
        _lang_code_to_name.put("dua", "Duala");
        _lang_code_to_name.put("dum", "Dutch, Middle (ca. 1050-1350)");
        _lang_code_to_name.put("nl", "Dutch; Flemish");
        _lang_code_to_name.put("dyu", "Dyula");
        _lang_code_to_name.put("dz", "Dzongkha");
        _lang_code_to_name.put("efi", "Efik");
        _lang_code_to_name.put("egy", "Egyptian (Ancient)");
        _lang_code_to_name.put("eka", "Ekajuk");
        _lang_code_to_name.put("elx", "Elamite");
        _lang_code_to_name.put("en", "English");
        _lang_code_to_name.put("enm", "English, Middle (1100-1500)");
        _lang_code_to_name.put("eo", "Esperanto");
        _lang_code_to_name.put("et", "Estonian");
        _lang_code_to_name.put("ee", "Ewe");
        _lang_code_to_name.put("ewo", "Ewondo");
        _lang_code_to_name.put("fan", "Fang");
        _lang_code_to_name.put("fo", "Faroese");
        _lang_code_to_name.put("fat", "Fanti");
        _lang_code_to_name.put("fj", "Fijian");
        _lang_code_to_name.put("fil", "Filipino; Pilipino");
        _lang_code_to_name.put("fi", "Finnish");
        _lang_code_to_name.put("fiu", "Finno-Ugrian languages");
        _lang_code_to_name.put("fon", "Fon");
        _lang_code_to_name.put("fr", "French");
        _lang_code_to_name.put("frm", "French, Middle (ca. 1400-1600)");
        _lang_code_to_name.put("fro", "French, Old (842-ca. 1400)");
        _lang_code_to_name.put("frr", "Northern Frisian");
        _lang_code_to_name.put("frs", "Eastern Frisian");
        _lang_code_to_name.put("fy", "Western Frisian");
        _lang_code_to_name.put("ff", "Fulah");
        _lang_code_to_name.put("fur", "Friulian");
        _lang_code_to_name.put("gaa", "Ga");
        _lang_code_to_name.put("gay", "Gayo");
        _lang_code_to_name.put("gba", "Gbaya");
        _lang_code_to_name.put("gem", "Germanic languages");
        _lang_code_to_name.put("ka", "Georgian");
        _lang_code_to_name.put("de", "German");
        _lang_code_to_name.put("gez", "Geez");
        _lang_code_to_name.put("gil", "Gilbertese");
        _lang_code_to_name.put("gd", "Gaelic; Scottish Gaelic");
        _lang_code_to_name.put("ga", "Irish");
        _lang_code_to_name.put("gl", "Galician");
        _lang_code_to_name.put("gv", "Manx");
        _lang_code_to_name.put("gmh", "German, Middle High (ca. 1050-1500)");
        _lang_code_to_name.put("goh", "German, Old High (ca. 750-1050)");
        _lang_code_to_name.put("gon", "Gondi");
        _lang_code_to_name.put("gor", "Gorontalo");
        _lang_code_to_name.put("got", "Gothic");
        _lang_code_to_name.put("grb", "Grebo");
        _lang_code_to_name.put("grc", "Greek, Ancient (to 1453)");
        _lang_code_to_name.put("el", "Greek, Modern (1453-)");
        _lang_code_to_name.put("gn", "Guarani");
        _lang_code_to_name.put("gsw", "Swiss German; Alemannic; Alsatian");
        _lang_code_to_name.put("gu", "Gujarati");
        _lang_code_to_name.put("gwi", "Gwich'in");
        _lang_code_to_name.put("hai", "Haida");
        _lang_code_to_name.put("ht", "Haitian; Haitian Creole");
        _lang_code_to_name.put("ha", "Hausa");
        _lang_code_to_name.put("haw", "Hawaiian");
        _lang_code_to_name.put("he", "Hebrew");
        _lang_code_to_name.put("hz", "Herero");
        _lang_code_to_name.put("hil", "Hiligaynon");
        _lang_code_to_name.put("him", "Himachali languages; Western Pahari languages");
        _lang_code_to_name.put("hi", "Hindi");
        _lang_code_to_name.put("hit", "Hittite");
        _lang_code_to_name.put("hmn", "Hmong; Mong");
        _lang_code_to_name.put("ho", "Hiri Motu");
        _lang_code_to_name.put("hr", "Croatian");
        _lang_code_to_name.put("hsb", "Upper Sorbian");
        _lang_code_to_name.put("hu", "Hungarian");
        _lang_code_to_name.put("hup", "Hupa");
        _lang_code_to_name.put("iba", "Iban");
        _lang_code_to_name.put("ig", "Igbo");
        _lang_code_to_name.put("is", "Icelandic");
        _lang_code_to_name.put("io", "Ido");
        _lang_code_to_name.put("ii", "Sichuan Yi; Nuosu");
        _lang_code_to_name.put("ijo", "Ijo languages");
        _lang_code_to_name.put("iu", "Inuktitut");
        _lang_code_to_name.put("ie", "Interlingue; Occidental");
        _lang_code_to_name.put("ilo", "Iloko");
        _lang_code_to_name.put("ia", "Interlingua (International Auxiliary Language Association)");
        _lang_code_to_name.put("inc", "Indic languages");
        _lang_code_to_name.put("id", "Indonesian");
        _lang_code_to_name.put("ine", "Indo-European languages");
        _lang_code_to_name.put("inh", "Ingush");
        _lang_code_to_name.put("ik", "Inupiaq");
        _lang_code_to_name.put("ira", "Iranian languages");
        _lang_code_to_name.put("iro", "Iroquoian languages");
        _lang_code_to_name.put("it", "Italian");
        _lang_code_to_name.put("jv", "Javanese");
        _lang_code_to_name.put("jbo", "Lojban");
        _lang_code_to_name.put("ja", "Japanese");
        _lang_code_to_name.put("jpr", "Judeo-Persian");
        _lang_code_to_name.put("jrb", "Judeo-Arabic");
        _lang_code_to_name.put("kaa", "Kara-Kalpak");
        _lang_code_to_name.put("kab", "Kabyle");
        _lang_code_to_name.put("kac", "Kachin; Jingpho");
        _lang_code_to_name.put("kl", "Kalaallisut; Greenlandic");
        _lang_code_to_name.put("kam", "Kamba");
        _lang_code_to_name.put("kn", "Kannada");
        _lang_code_to_name.put("kar", "Karen languages");
        _lang_code_to_name.put("ks", "Kashmiri");
        _lang_code_to_name.put("kr", "Kanuri");
        _lang_code_to_name.put("kaw", "Kawi");
        _lang_code_to_name.put("kk", "Kazakh");
        _lang_code_to_name.put("kbd", "Kabardian");
        _lang_code_to_name.put("kha", "Khasi");
        _lang_code_to_name.put("khi", "Khoisan languages");
        _lang_code_to_name.put("km", "Central Khmer");
        _lang_code_to_name.put("kho", "Khotanese;Sakan");
        _lang_code_to_name.put("ki", "Kikuyu; Gikuyu");
        _lang_code_to_name.put("rw", "Kinyarwanda");
        _lang_code_to_name.put("ky", "Kirghiz; Kyrgyz");
        _lang_code_to_name.put("kmb", "Kimbundu");
        _lang_code_to_name.put("kok", "Konkani");
        _lang_code_to_name.put("kv", "Komi");
        _lang_code_to_name.put("kg", "Kongo");
        _lang_code_to_name.put("ko", "Korean");
        _lang_code_to_name.put("kos", "Kosraean");
        _lang_code_to_name.put("kpe", "Kpelle");
        _lang_code_to_name.put("krc", "Karachay-Balkar");
        _lang_code_to_name.put("krl", "Karelian");
        _lang_code_to_name.put("kro", "Kru languages");
        _lang_code_to_name.put("kru", "Kurukh");
        _lang_code_to_name.put("kj", "Kuanyama; Kwanyama");
        _lang_code_to_name.put("kum", "Kumyk");
        _lang_code_to_name.put("ku", "Kurdish");
        _lang_code_to_name.put("kut", "Kutenai");
        _lang_code_to_name.put("lad", "Ladino");
        _lang_code_to_name.put("lah", "Lahnda");
        _lang_code_to_name.put("lam", "Lamba");
        _lang_code_to_name.put("lo", "Lao");
        _lang_code_to_name.put("la", "Latin");
        _lang_code_to_name.put("lv", "Latvian");
        _lang_code_to_name.put("lez", "Lezghian");
        _lang_code_to_name.put("li", "Limburgan; Limburger; Limburgish");
        _lang_code_to_name.put("ln", "Lingala");
        _lang_code_to_name.put("lt", "Lithuanian");
        _lang_code_to_name.put("lol", "Mongo");
        _lang_code_to_name.put("loz", "Lozi");
        _lang_code_to_name.put("lb", "Luxembourgish; Letzeburgesch");
        _lang_code_to_name.put("lua", "Luba-Lulua");
        _lang_code_to_name.put("lu", "Luba-Katanga");
        _lang_code_to_name.put("lg", "Ganda");
        _lang_code_to_name.put("lui", "Luiseno");
        _lang_code_to_name.put("lun", "Lunda");
        _lang_code_to_name.put("luo", "Luo (Kenya and Tanzania)");
        _lang_code_to_name.put("lus", "Lushai");
        _lang_code_to_name.put("mk", "Macedonian");
        _lang_code_to_name.put("mad", "Madurese");
        _lang_code_to_name.put("mag", "Magahi");
        _lang_code_to_name.put("mh", "Marshallese");
        _lang_code_to_name.put("mai", "Maithili");
        _lang_code_to_name.put("mak", "Makasar");
        _lang_code_to_name.put("ml", "Malayalam");
        _lang_code_to_name.put("man", "Mandingo");
        _lang_code_to_name.put("mi", "Maori");
        _lang_code_to_name.put("map", "Austronesian languages");
        _lang_code_to_name.put("mr", "Marathi");
        _lang_code_to_name.put("mas", "Masai");
        _lang_code_to_name.put("ms", "Malay");
        _lang_code_to_name.put("mdf", "Moksha");
        _lang_code_to_name.put("mdr", "Mandar");
        _lang_code_to_name.put("men", "Mende");
        _lang_code_to_name.put("mga", "Irish, Middle (900-1200)");
        _lang_code_to_name.put("mic", "Mi'kmaq; Micmac");
        _lang_code_to_name.put("min", "Minangkabau");
        _lang_code_to_name.put("mis", "Uncoded languages");
        _lang_code_to_name.put("mkh", "Mon-Khmer languages");
        _lang_code_to_name.put("mg", "Malagasy");
        _lang_code_to_name.put("mt", "Maltese");
        _lang_code_to_name.put("mnc", "Manchu");
        _lang_code_to_name.put("mni", "Manipuri");
        _lang_code_to_name.put("mno", "Manobo languages");
        _lang_code_to_name.put("moh", "Mohawk");
        _lang_code_to_name.put("mo", "Moldavian; Moldovan");
        _lang_code_to_name.put("mn", "Mongolian");
        _lang_code_to_name.put("mos", "Mossi");
        _lang_code_to_name.put("mul", "Multiple languages");
        _lang_code_to_name.put("mun", "Munda languages");
        _lang_code_to_name.put("mus", "Creek");
        _lang_code_to_name.put("mwl", "Mirandese");
        _lang_code_to_name.put("mwr", "Marwari");
        _lang_code_to_name.put("myn", "Mayan languages");
        _lang_code_to_name.put("myv", "Erzya");
        _lang_code_to_name.put("nah", "Nahuatl languages");
        _lang_code_to_name.put("nai", "North American Indian languages");
        _lang_code_to_name.put("nap", "Neapolitan");
        _lang_code_to_name.put("na", "Nauru");
        _lang_code_to_name.put("nv", "Navajo; Navaho");
        _lang_code_to_name.put("nr", "Ndebele, South; South Ndebele");
        _lang_code_to_name.put("nd", "Ndebele, North; North Ndebele");
        _lang_code_to_name.put("ng", "Ndonga");
        _lang_code_to_name.put("nds", "Low German; Low Saxon; German, Low; Saxon, Low");
        _lang_code_to_name.put("ne", "Nepali");
        _lang_code_to_name.put("new", "Nepal Bhasa; Newari");
        _lang_code_to_name.put("nia", "Nias");
        _lang_code_to_name.put("nic", "Niger-Kordofanian languages");
        _lang_code_to_name.put("niu", "Niuean");
        _lang_code_to_name.put("nn", "Norwegian Nynorsk; Nynorsk, Norwegian");
        _lang_code_to_name.put("nb", "BokmC%l, Norwegian; Norwegian BokmC%l");
        _lang_code_to_name.put("nog", "Nogai");
        _lang_code_to_name.put("non", "Norse, Old");
        _lang_code_to_name.put("no", "Norwegian");
        _lang_code_to_name.put("nqo", "N'Ko");
        _lang_code_to_name.put("nso", "Pedi; Sepedi; Northern Sotho");
        _lang_code_to_name.put("nub", "Nubian languages");
        _lang_code_to_name.put("nwc", "Classical Newari; Old Newari; Classical Nepal Bhasa");
        _lang_code_to_name.put("ny", "Chichewa; Chewa; Nyanja");
        _lang_code_to_name.put("nym", "Nyamwezi");
        _lang_code_to_name.put("nyn", "Nyankole");
        _lang_code_to_name.put("nyo", "Nyoro");
        _lang_code_to_name.put("nzi", "Nzima");
        _lang_code_to_name.put("oc", "Occitan (post 1500)");
        _lang_code_to_name.put("oj", "Ojibwa");
        _lang_code_to_name.put("or", "Oriya");
        _lang_code_to_name.put("om", "Oromo");
        _lang_code_to_name.put("osa", "Osage");
        _lang_code_to_name.put("os", "Ossetian; Ossetic");
        _lang_code_to_name.put("ota", "Turkish, Ottoman (1500-1928)");
        _lang_code_to_name.put("oto", "Otomian languages");
        _lang_code_to_name.put("paa", "Papuan languages");
        _lang_code_to_name.put("pag", "Pangasinan");
        _lang_code_to_name.put("pal", "Pahlavi");
        _lang_code_to_name.put("pam", "Pampanga; Kapampangan");
        _lang_code_to_name.put("pa", "Panjabi; Punjabi");
        _lang_code_to_name.put("pap", "Papiamento");
        _lang_code_to_name.put("pau", "Palauan");
        _lang_code_to_name.put("peo", "Persian, Old (ca. 600-400 B.C.)");
        _lang_code_to_name.put("fa", "Persian");
        _lang_code_to_name.put("phi", "Philippine languages");
        _lang_code_to_name.put("phn", "Phoenician");
        _lang_code_to_name.put("pi", "Pali");
        _lang_code_to_name.put("pl", "Polish");
        _lang_code_to_name.put("pon", "Pohnpeian");
        _lang_code_to_name.put("pt", "Portuguese");
        _lang_code_to_name.put("pra", "Prakrit languages");
        _lang_code_to_name.put("pro", "ProvenC'al, Old (to 1500); Occitan, Old (to 1500)");
        _lang_code_to_name.put("ps", "Pushto; Pashto");
        _lang_code_to_name.put("qu", "Quechua");
        _lang_code_to_name.put("raj", "Rajasthani");
        _lang_code_to_name.put("rap", "Rapanui");
        _lang_code_to_name.put("rar", "Rarotongan; Cook Islands Maori");
        _lang_code_to_name.put("roa", "Romance languages");
        _lang_code_to_name.put("rm", "Romansh");
        _lang_code_to_name.put("rom", "Romany");
        _lang_code_to_name.put("ro", "Romanian");
        _lang_code_to_name.put("rn", "Rundi");
        _lang_code_to_name.put("rup", "Aromanian; Arumanian; Macedo-Romanian");
        _lang_code_to_name.put("ru", "Russian");
        _lang_code_to_name.put("sad", "Sandawe");
        _lang_code_to_name.put("sg", "Sango");
        _lang_code_to_name.put("sah", "Yakut");
        _lang_code_to_name.put("sai", "South American Indian languages");
        _lang_code_to_name.put("sal", "Salishan languages");
        _lang_code_to_name.put("sam", "Samaritan Aramaic");
        _lang_code_to_name.put("sa", "Sanskrit");
        _lang_code_to_name.put("sas", "Sasak");
        _lang_code_to_name.put("sat", "Santali");
        _lang_code_to_name.put("scn", "Sicilian");
        _lang_code_to_name.put("sco", "Scots");
        _lang_code_to_name.put("sel", "Selkup");
        _lang_code_to_name.put("sem", "Semitic languages");
        _lang_code_to_name.put("sga", "Irish, Old (to 900)");
        _lang_code_to_name.put("sgn", "Sign Languages");
        _lang_code_to_name.put("shn", "Shan");
        _lang_code_to_name.put("sid", "Sidamo");
        _lang_code_to_name.put("si", "Sinhala; Sinhalese");
        _lang_code_to_name.put("sio", "Siouan languages");
        _lang_code_to_name.put("sit", "Sino-Tibetan languages");
        _lang_code_to_name.put("sla", "Slavic languages");
        _lang_code_to_name.put("sk", "Slovak");
        _lang_code_to_name.put("sl", "Slovenian");
        _lang_code_to_name.put("sma", "Southern Sami");
        _lang_code_to_name.put("se", "Northern Sami");
        _lang_code_to_name.put("smi", "Sami languages");
        _lang_code_to_name.put("smj", "Lule Sami");
        _lang_code_to_name.put("smn", "Inari Sami");
        _lang_code_to_name.put("sm", "Samoan");
        _lang_code_to_name.put("sms", "Skolt Sami");
        _lang_code_to_name.put("sn", "Shona");
        _lang_code_to_name.put("sd", "Sindhi");
        _lang_code_to_name.put("snk", "Soninke");
        _lang_code_to_name.put("sog", "Sogdian");
        _lang_code_to_name.put("so", "Somali");
        _lang_code_to_name.put("son", "Songhai languages");
        _lang_code_to_name.put("st", "Sotho, Southern");
        _lang_code_to_name.put("es", "Spanish; Castilian");
        _lang_code_to_name.put("sc", "Sardinian");
        _lang_code_to_name.put("srn", "Sranan Tongo");
        _lang_code_to_name.put("sr", "Serbian");
        _lang_code_to_name.put("srr", "Serer");
        _lang_code_to_name.put("ssa", "Nilo-Saharan languages");
        _lang_code_to_name.put("ss", "Swati");
        _lang_code_to_name.put("suk", "Sukuma");
        _lang_code_to_name.put("su", "Sundanese");
        _lang_code_to_name.put("sus", "Susu");
        _lang_code_to_name.put("sux", "Sumerian");
        _lang_code_to_name.put("sw", "Swahili");
        _lang_code_to_name.put("sv", "Swedish");
        _lang_code_to_name.put("syc", "Classical Syriac");
        _lang_code_to_name.put("syr", "Syriac");
        _lang_code_to_name.put("ty", "Tahitian");
        _lang_code_to_name.put("tai", "Tai languages");
        _lang_code_to_name.put("ta", "Tamil");
        _lang_code_to_name.put("tt", "Tatar");
        _lang_code_to_name.put("te", "Telugu");
        _lang_code_to_name.put("tem", "Timne");
        _lang_code_to_name.put("ter", "Tereno");
        _lang_code_to_name.put("tet", "Tetum");
        _lang_code_to_name.put("tg", "Tajik");
        _lang_code_to_name.put("tl", "Tagalog");
        _lang_code_to_name.put("th", "Thai");
        _lang_code_to_name.put("bo", "Tibetan");
        _lang_code_to_name.put("tig", "Tigre");
        _lang_code_to_name.put("ti", "Tigrinya");
        _lang_code_to_name.put("tiv", "Tiv");
        _lang_code_to_name.put("tkl", "Tokelau");
        _lang_code_to_name.put("tlh", "Klingon; tlhIngan-Hol");
        _lang_code_to_name.put("tli", "Tlingit");
        _lang_code_to_name.put("tmh", "Tamashek");
        _lang_code_to_name.put("tog", "Tonga (Nyasa)");
        _lang_code_to_name.put("to", "Tonga (Tonga Islands)");
        _lang_code_to_name.put("tpi", "Tok Pisin");
        _lang_code_to_name.put("tsi", "Tsimshian");
        _lang_code_to_name.put("tn", "Tswana");
        _lang_code_to_name.put("ts", "Tsonga");
        _lang_code_to_name.put("tk", "Turkmen");
        _lang_code_to_name.put("tum", "Tumbuka");
        _lang_code_to_name.put("tup", "Tupi languages");
        _lang_code_to_name.put("tr", "Turkish");
        _lang_code_to_name.put("tut", "Altaic languages");
        _lang_code_to_name.put("tvl", "Tuvalu");
        _lang_code_to_name.put("tw", "Twi");
        _lang_code_to_name.put("tyv", "Tuvinian");
        _lang_code_to_name.put("udm", "Udmurt");
        _lang_code_to_name.put("uga", "Ugaritic");
        _lang_code_to_name.put("ug", "Uighur; Uyghur");
        _lang_code_to_name.put("uk", "Ukrainian");
        _lang_code_to_name.put("umb", "Umbundu");
        _lang_code_to_name.put("und", "Undetermined");
        _lang_code_to_name.put("ur", "Urdu");
        _lang_code_to_name.put("uz", "Uzbek");
        _lang_code_to_name.put("vai", "Vai");
        _lang_code_to_name.put("ve", "Venda");
        _lang_code_to_name.put("vi", "Vietnamese");
        _lang_code_to_name.put("vo", "VolapC<k");
        _lang_code_to_name.put("vot", "Votic");
        _lang_code_to_name.put("wak", "Wakashan languages");
        _lang_code_to_name.put("wal", "Wolaitta; Wolaytta");
        _lang_code_to_name.put("war", "Waray");
        _lang_code_to_name.put("was", "Washo");
        _lang_code_to_name.put("cy", "Welsh");
        _lang_code_to_name.put("wen", "Sorbian languages");
        _lang_code_to_name.put("wa", "Walloon");
        _lang_code_to_name.put("wo", "Wolof");
        _lang_code_to_name.put("xal", "Kalmyk; Oirat");
        _lang_code_to_name.put("xh", "Xhosa");
        _lang_code_to_name.put("yao", "Yao");
        _lang_code_to_name.put("yap", "Yapese");
        _lang_code_to_name.put("yi", "Yiddish");
        _lang_code_to_name.put("yo", "Yoruba");
        _lang_code_to_name.put("ypk", "Yupik languages");
        _lang_code_to_name.put("zap", "Zapotec");
        _lang_code_to_name.put("zbl", "Blissymbols; Blissymbolics; Bliss");
        _lang_code_to_name.put("zen", "Zenaga");
        _lang_code_to_name.put("za", "Zhuang; Chuang");
        _lang_code_to_name.put("znd", "Zande languages");
        _lang_code_to_name.put("zu", "Zulu");
        _lang_code_to_name.put("zun", "Zuni");
        _lang_code_to_name.put("zza", "Zaza; Dimili; Dimli; Kirdki; Kirmanjki; Zazaki");

        _lang_name_to_code.put("Afar", "aa");
        _lang_name_to_code.put("Abkhazian", "ab");
        _lang_name_to_code.put("Achinese", "ace");
        _lang_name_to_code.put("Acoli", "ach");
        _lang_name_to_code.put("Adangme", "ada");
        _lang_name_to_code.put("Adyghe; Adygei", "ady");
        _lang_name_to_code.put("Afro-Asiatic languages", "afa");
        _lang_name_to_code.put("Afrihili", "afh");
        _lang_name_to_code.put("Afrikaans", "af");
        _lang_name_to_code.put("Ainu", "ain");
        _lang_name_to_code.put("Akan", "ak");
        _lang_name_to_code.put("Akkadian", "akk");
        _lang_name_to_code.put("Albanian", "sq");
        _lang_name_to_code.put("Aleut", "ale");
        _lang_name_to_code.put("Algonquian languages", "alg");
        _lang_name_to_code.put("Southern Altai", "alt");
        _lang_name_to_code.put("Amharic", "am");
        _lang_name_to_code.put("English, Old (ca. 450-1100)", "ang");
        _lang_name_to_code.put("Angika", "anp");
        _lang_name_to_code.put("Apache languages", "apa");
        _lang_name_to_code.put("Arabic", "ar");
        _lang_name_to_code.put("Official Aramaic (700-300 BCE); Imperial Aramaic (700-300 BCE)", "arc");
        _lang_name_to_code.put("Aragonese", "an");
        _lang_name_to_code.put("Armenian", "hy");
        _lang_name_to_code.put("Mapudungun; Mapuche", "arn");
        _lang_name_to_code.put("Arapaho", "arp");
        _lang_name_to_code.put("Artificial languages", "art");
        _lang_name_to_code.put("Arawak", "arw");
        _lang_name_to_code.put("Assamese", "as");
        _lang_name_to_code.put("Asturian; Bable; Leonese; Asturleonese", "ast");
        _lang_name_to_code.put("Athapascan languages", "ath");
        _lang_name_to_code.put("Australian languages", "aus");
        _lang_name_to_code.put("Avaric", "av");
        _lang_name_to_code.put("Avestan", "ae");
        _lang_name_to_code.put("Awadhi", "awa");
        _lang_name_to_code.put("Aymara", "ay");
        _lang_name_to_code.put("Azerbaijani", "az");
        _lang_name_to_code.put("Banda languages", "bad");
        _lang_name_to_code.put("Bamileke languages", "bai");
        _lang_name_to_code.put("Bashkir", "ba");
        _lang_name_to_code.put("Baluchi", "bal");
        _lang_name_to_code.put("Bambara", "bm");
        _lang_name_to_code.put("Balinese", "ban");
        _lang_name_to_code.put("Basque", "eu");
        _lang_name_to_code.put("Basa", "bas");
        _lang_name_to_code.put("Baltic languages", "bat");
        _lang_name_to_code.put("Beja; Bedawiyet", "bej");
        _lang_name_to_code.put("Belarusian", "be");
        _lang_name_to_code.put("Bemba", "bem");
        _lang_name_to_code.put("Bengali", "bn");
        _lang_name_to_code.put("Berber languages", "ber");
        _lang_name_to_code.put("Bhojpuri", "bho");
        _lang_name_to_code.put("Bihari languages", "bh");
        _lang_name_to_code.put("Bikol", "bik");
        _lang_name_to_code.put("Bini; Edo", "bin");
        _lang_name_to_code.put("Bislama", "bi");
        _lang_name_to_code.put("Siksika", "bla");
        _lang_name_to_code.put("Bantu languages", "bnt");
        _lang_name_to_code.put("Bosnian", "bs");
        _lang_name_to_code.put("Braj", "bra");
        _lang_name_to_code.put("Breton", "br");
        _lang_name_to_code.put("Batak languages", "btk");
        _lang_name_to_code.put("Buriat", "bua");
        _lang_name_to_code.put("Buginese", "bug");
        _lang_name_to_code.put("Bulgarian", "bg");
        _lang_name_to_code.put("Burmese", "my");
        _lang_name_to_code.put("Blin; Bilin", "byn");
        _lang_name_to_code.put("Caddo", "cad");
        _lang_name_to_code.put("Central American Indian languages", "cai");
        _lang_name_to_code.put("Galibi Carib", "car");
        _lang_name_to_code.put("Catalan; Valencian", "ca");
        _lang_name_to_code.put("Caucasian languages", "cau");
        _lang_name_to_code.put("Cebuano", "ceb");
        _lang_name_to_code.put("Celtic languages", "cel");
        _lang_name_to_code.put("Chamorro", "ch");
        _lang_name_to_code.put("Chibcha", "chb");
        _lang_name_to_code.put("Chechen", "ce");
        _lang_name_to_code.put("Chagatai", "chg");
        _lang_name_to_code.put("Chinese", "zh");
        _lang_name_to_code.put("Chuukese", "chk");
        _lang_name_to_code.put("Mari", "chm");
        _lang_name_to_code.put("Chinook jargon", "chn");
        _lang_name_to_code.put("Choctaw", "cho");
        _lang_name_to_code.put("Chipewyan; Dene Suline", "chp");
        _lang_name_to_code.put("Cherokee", "chr");
        _lang_name_to_code.put("Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic", "cu");
        _lang_name_to_code.put("Chuvash", "cv");
        _lang_name_to_code.put("Cheyenne", "chy");
        _lang_name_to_code.put("Chamic languages", "cmc");
        _lang_name_to_code.put("Coptic", "cop");
        _lang_name_to_code.put("Cornish", "kw");
        _lang_name_to_code.put("Corsican", "co");
        _lang_name_to_code.put("Creoles and pidgins, English based", "cpe");
        _lang_name_to_code.put("Creoles and pidgins, French-based", "cpf");
        _lang_name_to_code.put("Creoles and pidgins, Portuguese-based", "cpp");
        _lang_name_to_code.put("Cree", "cr");
        _lang_name_to_code.put("Crimean Tatar; Crimean Turkish", "crh");
        _lang_name_to_code.put("Creoles and pidgins", "crp");
        _lang_name_to_code.put("Kashubian", "csb");
        _lang_name_to_code.put("Cushitic languages", "cus");
        _lang_name_to_code.put("Czech", "cs");
        _lang_name_to_code.put("Dakota", "dak");
        _lang_name_to_code.put("Danish", "da");
        _lang_name_to_code.put("Dargwa", "dar");
        _lang_name_to_code.put("Land Dayak languages", "day");
        _lang_name_to_code.put("Delaware", "del");
        _lang_name_to_code.put("Slave (Athapascan)", "den");
        _lang_name_to_code.put("Dogrib", "dgr");
        _lang_name_to_code.put("Dinka", "din");
        _lang_name_to_code.put("Divehi; Dhivehi; Maldivian", "dv");
        _lang_name_to_code.put("Dogri", "doi");
        _lang_name_to_code.put("Dravidian languages", "dra");
        _lang_name_to_code.put("Lower Sorbian", "dsb");
        _lang_name_to_code.put("Duala", "dua");
        _lang_name_to_code.put("Dutch, Middle (ca. 1050-1350)", "dum");
        _lang_name_to_code.put("Dutch; Flemish", "nl");
        _lang_name_to_code.put("Dyula", "dyu");
        _lang_name_to_code.put("Dzongkha", "dz");
        _lang_name_to_code.put("Efik", "efi");
        _lang_name_to_code.put("Egyptian (Ancient)", "egy");
        _lang_name_to_code.put("Ekajuk", "eka");
        _lang_name_to_code.put("Elamite", "elx");
        _lang_name_to_code.put("English", "en");
        _lang_name_to_code.put("English, Middle (1100-1500)", "enm");
        _lang_name_to_code.put("Esperanto", "eo");
        _lang_name_to_code.put("Estonian", "et");
        _lang_name_to_code.put("Ewe", "ee");
        _lang_name_to_code.put("Ewondo", "ewo");
        _lang_name_to_code.put("Fang", "fan");
        _lang_name_to_code.put("Faroese", "fo");
        _lang_name_to_code.put("Fanti", "fat");
        _lang_name_to_code.put("Fijian", "fj");
        _lang_name_to_code.put("Filipino; Pilipino", "fil");
        _lang_name_to_code.put("Finnish", "fi");
        _lang_name_to_code.put("Finno-Ugrian languages", "fiu");
        _lang_name_to_code.put("Fon", "fon");
        _lang_name_to_code.put("French", "fr");
        _lang_name_to_code.put("French, Middle (ca. 1400-1600)", "frm");
        _lang_name_to_code.put("French, Old (842-ca. 1400)", "fro");
        _lang_name_to_code.put("Northern Frisian", "frr");
        _lang_name_to_code.put("Eastern Frisian", "frs");
        _lang_name_to_code.put("Western Frisian", "fy");
        _lang_name_to_code.put("Fulah", "ff");
        _lang_name_to_code.put("Friulian", "fur");
        _lang_name_to_code.put("Ga", "gaa");
        _lang_name_to_code.put("Gayo", "gay");
        _lang_name_to_code.put("Gbaya", "gba");
        _lang_name_to_code.put("Germanic languages", "gem");
        _lang_name_to_code.put("Georgian", "ka");
        _lang_name_to_code.put("German", "de");
        _lang_name_to_code.put("Geez", "gez");
        _lang_name_to_code.put("Gilbertese", "gil");
        _lang_name_to_code.put("Gaelic; Scottish Gaelic", "gd");
        _lang_name_to_code.put("Irish", "ga");
        _lang_name_to_code.put("Galician", "gl");
        _lang_name_to_code.put("Manx", "gv");
        _lang_name_to_code.put("German, Middle High (ca. 1050-1500)", "gmh");
        _lang_name_to_code.put("German, Old High (ca. 750-1050)", "goh");
        _lang_name_to_code.put("Gondi", "gon");
        _lang_name_to_code.put("Gorontalo", "gor");
        _lang_name_to_code.put("Gothic", "got");
        _lang_name_to_code.put("Grebo", "grb");
        _lang_name_to_code.put("Greek, Ancient (to 1453)", "grc");
        _lang_name_to_code.put("Greek, Modern (1453-)", "el");
        _lang_name_to_code.put("Guarani", "gn");
        _lang_name_to_code.put("Swiss German; Alemannic; Alsatian", "gsw");
        _lang_name_to_code.put("Gujarati", "gu");
        _lang_name_to_code.put("Gwich'in", "gwi");
        _lang_name_to_code.put("Haida", "hai");
        _lang_name_to_code.put("Haitian; Haitian Creole", "ht");
        _lang_name_to_code.put("Hausa", "ha");
        _lang_name_to_code.put("Hawaiian", "haw");
        _lang_name_to_code.put("Hebrew", "he");
        _lang_name_to_code.put("Herero", "hz");
        _lang_name_to_code.put("Hiligaynon", "hil");
        _lang_name_to_code.put("Himachali languages; Western Pahari languages", "him");
        _lang_name_to_code.put("Hindi", "hi");
        _lang_name_to_code.put("Hittite", "hit");
        _lang_name_to_code.put("Hmong; Mong", "hmn");
        _lang_name_to_code.put("Hiri Motu", "ho");
        _lang_name_to_code.put("Croatian", "hr");
        _lang_name_to_code.put("Upper Sorbian", "hsb");
        _lang_name_to_code.put("Hungarian", "hu");
        _lang_name_to_code.put("Hupa", "hup");
        _lang_name_to_code.put("Iban", "iba");
        _lang_name_to_code.put("Igbo", "ig");
        _lang_name_to_code.put("Icelandic", "is");
        _lang_name_to_code.put("Ido", "io");
        _lang_name_to_code.put("Sichuan Yi; Nuosu", "ii");
        _lang_name_to_code.put("Ijo languages", "ijo");
        _lang_name_to_code.put("Inuktitut", "iu");
        _lang_name_to_code.put("Interlingue; Occidental", "ie");
        _lang_name_to_code.put("Iloko", "ilo");
        _lang_name_to_code.put("Interlingua (International Auxiliary Language Association)", "ia");
        _lang_name_to_code.put("Indic languages", "inc");
        _lang_name_to_code.put("Indonesian", "id");
        _lang_name_to_code.put("Indo-European languages", "ine");
        _lang_name_to_code.put("Ingush", "inh");
        _lang_name_to_code.put("Inupiaq", "ik");
        _lang_name_to_code.put("Iranian languages", "ira");
        _lang_name_to_code.put("Iroquoian languages", "iro");
        _lang_name_to_code.put("Italian", "it");
        _lang_name_to_code.put("Javanese", "jv");
        _lang_name_to_code.put("Lojban", "jbo");
        _lang_name_to_code.put("Japanese", "ja");
        _lang_name_to_code.put("Judeo-Persian", "jpr");
        _lang_name_to_code.put("Judeo-Arabic", "jrb");
        _lang_name_to_code.put("Kara-Kalpak", "kaa");
        _lang_name_to_code.put("Kabyle", "kab");
        _lang_name_to_code.put("Kachin; Jingpho", "kac");
        _lang_name_to_code.put("Kalaallisut; Greenlandic", "kl");
        _lang_name_to_code.put("Kamba", "kam");
        _lang_name_to_code.put("Kannada", "kn");
        _lang_name_to_code.put("Karen languages", "kar");
        _lang_name_to_code.put("Kashmiri", "ks");
        _lang_name_to_code.put("Kanuri", "kr");
        _lang_name_to_code.put("Kawi", "kaw");
        _lang_name_to_code.put("Kazakh", "kk");
        _lang_name_to_code.put("Kabardian", "kbd");
        _lang_name_to_code.put("Khasi", "kha");
        _lang_name_to_code.put("Khoisan languages", "khi");
        _lang_name_to_code.put("Central Khmer", "km");
        _lang_name_to_code.put("Khotanese;Sakan", "kho");
        _lang_name_to_code.put("Kikuyu; Gikuyu", "ki");
        _lang_name_to_code.put("Kinyarwanda", "rw");
        _lang_name_to_code.put("Kirghiz; Kyrgyz", "ky");
        _lang_name_to_code.put("Kimbundu", "kmb");
        _lang_name_to_code.put("Konkani", "kok");
        _lang_name_to_code.put("Komi", "kv");
        _lang_name_to_code.put("Kongo", "kg");
        _lang_name_to_code.put("Korean", "ko");
        _lang_name_to_code.put("Kosraean", "kos");
        _lang_name_to_code.put("Kpelle", "kpe");
        _lang_name_to_code.put("Karachay-Balkar", "krc");
        _lang_name_to_code.put("Karelian", "krl");
        _lang_name_to_code.put("Kru languages", "kro");
        _lang_name_to_code.put("Kurukh", "kru");
        _lang_name_to_code.put("Kuanyama; Kwanyama", "kj");
        _lang_name_to_code.put("Kumyk", "kum");
        _lang_name_to_code.put("Kurdish", "ku");
        _lang_name_to_code.put("Kutenai", "kut");
        _lang_name_to_code.put("Ladino", "lad");
        _lang_name_to_code.put("Lahnda", "lah");
        _lang_name_to_code.put("Lamba", "lam");
        _lang_name_to_code.put("Lao", "lo");
        _lang_name_to_code.put("Latin", "la");
        _lang_name_to_code.put("Latvian", "lv");
        _lang_name_to_code.put("Lezghian", "lez");
        _lang_name_to_code.put("Limburgan; Limburger; Limburgish", "li");
        _lang_name_to_code.put("Lingala", "ln");
        _lang_name_to_code.put("Lithuanian", "lt");
        _lang_name_to_code.put("Mongo", "lol");
        _lang_name_to_code.put("Lozi", "loz");
        _lang_name_to_code.put("Luxembourgish; Letzeburgesch", "lb");
        _lang_name_to_code.put("Luba-Lulua", "lua");
        _lang_name_to_code.put("Luba-Katanga", "lu");
        _lang_name_to_code.put("Ganda", "lg");
        _lang_name_to_code.put("Luiseno", "lui");
        _lang_name_to_code.put("Lunda", "lun");
        _lang_name_to_code.put("Luo (Kenya and Tanzania)", "luo");
        _lang_name_to_code.put("Lushai", "lus");
        _lang_name_to_code.put("Macedonian", "mk");
        _lang_name_to_code.put("Madurese", "mad");
        _lang_name_to_code.put("Magahi", "mag");
        _lang_name_to_code.put("Marshallese", "mh");
        _lang_name_to_code.put("Maithili", "mai");
        _lang_name_to_code.put("Makasar", "mak");
        _lang_name_to_code.put("Malayalam", "ml");
        _lang_name_to_code.put("Mandingo", "man");
        _lang_name_to_code.put("Maori", "mi");
        _lang_name_to_code.put("Austronesian languages", "map");
        _lang_name_to_code.put("Marathi", "mr");
        _lang_name_to_code.put("Masai", "mas");
        _lang_name_to_code.put("Malay", "ms");
        _lang_name_to_code.put("Moksha", "mdf");
        _lang_name_to_code.put("Mandar", "mdr");
        _lang_name_to_code.put("Mende", "men");
        _lang_name_to_code.put("Irish, Middle (900-1200)", "mga");
        _lang_name_to_code.put("Mi'kmaq; Micmac", "mic");
        _lang_name_to_code.put("Minangkabau", "min");
        _lang_name_to_code.put("Uncoded languages", "mis");
        _lang_name_to_code.put("Mon-Khmer languages", "mkh");
        _lang_name_to_code.put("Malagasy", "mg");
        _lang_name_to_code.put("Maltese", "mt");
        _lang_name_to_code.put("Manchu", "mnc");
        _lang_name_to_code.put("Manipuri", "mni");
        _lang_name_to_code.put("Manobo languages", "mno");
        _lang_name_to_code.put("Mohawk", "moh");
        _lang_name_to_code.put("Moldavian; Moldovan", "mo");
        _lang_name_to_code.put("Mongolian", "mn");
        _lang_name_to_code.put("Mossi", "mos");
        _lang_name_to_code.put("Multiple languages", "mul");
        _lang_name_to_code.put("Munda languages", "mun");
        _lang_name_to_code.put("Creek", "mus");
        _lang_name_to_code.put("Mirandese", "mwl");
        _lang_name_to_code.put("Marwari", "mwr");
        _lang_name_to_code.put("Mayan languages", "myn");
        _lang_name_to_code.put("Erzya", "myv");
        _lang_name_to_code.put("Nahuatl languages", "nah");
        _lang_name_to_code.put("North American Indian languages", "nai");
        _lang_name_to_code.put("Neapolitan", "nap");
        _lang_name_to_code.put("Nauru", "na");
        _lang_name_to_code.put("Navajo; Navaho", "nv");
        _lang_name_to_code.put("Ndebele, South; South Ndebele", "nr");
        _lang_name_to_code.put("Ndebele, North; North Ndebele", "nd");
        _lang_name_to_code.put("Ndonga", "ng");
        _lang_name_to_code.put("Low German; Low Saxon; German, Low; Saxon, Low", "nds");
        _lang_name_to_code.put("Nepali", "ne");
        _lang_name_to_code.put("Nepal Bhasa; Newari", "new");
        _lang_name_to_code.put("Nias", "nia");
        _lang_name_to_code.put("Niger-Kordofanian languages", "nic");
        _lang_name_to_code.put("Niuean", "niu");
        _lang_name_to_code.put("Norwegian Nynorsk; Nynorsk, Norwegian", "nn");
        _lang_name_to_code.put("BokmC%l, Norwegian; Norwegian BokmC%l", "nb");
        _lang_name_to_code.put("Nogai", "nog");
        _lang_name_to_code.put("Norse, Old", "non");
        _lang_name_to_code.put("Norwegian", "no");
        _lang_name_to_code.put("N'Ko", "nqo");
        _lang_name_to_code.put("Pedi; Sepedi; Northern Sotho", "nso");
        _lang_name_to_code.put("Nubian languages", "nub");
        _lang_name_to_code.put("Classical Newari; Old Newari; Classical Nepal Bhasa", "nwc");
        _lang_name_to_code.put("Chichewa; Chewa; Nyanja", "ny");
        _lang_name_to_code.put("Nyamwezi", "nym");
        _lang_name_to_code.put("Nyankole", "nyn");
        _lang_name_to_code.put("Nyoro", "nyo");
        _lang_name_to_code.put("Nzima", "nzi");
        _lang_name_to_code.put("Occitan (post 1500)", "oc");
        _lang_name_to_code.put("Ojibwa", "oj");
        _lang_name_to_code.put("Oriya", "or");
        _lang_name_to_code.put("Oromo", "om");
        _lang_name_to_code.put("Osage", "osa");
        _lang_name_to_code.put("Ossetian; Ossetic", "os");
        _lang_name_to_code.put("Turkish, Ottoman (1500-1928)", "ota");
        _lang_name_to_code.put("Otomian languages", "oto");
        _lang_name_to_code.put("Papuan languages", "paa");
        _lang_name_to_code.put("Pangasinan", "pag");
        _lang_name_to_code.put("Pahlavi", "pal");
        _lang_name_to_code.put("Pampanga; Kapampangan", "pam");
        _lang_name_to_code.put("Panjabi; Punjabi", "pa");
        _lang_name_to_code.put("Papiamento", "pap");
        _lang_name_to_code.put("Palauan", "pau");
        _lang_name_to_code.put("Persian, Old (ca. 600-400 B.C.)", "peo");
        _lang_name_to_code.put("Persian", "fa");
        _lang_name_to_code.put("Philippine languages", "phi");
        _lang_name_to_code.put("Phoenician", "phn");
        _lang_name_to_code.put("Pali", "pi");
        _lang_name_to_code.put("Polish", "pl");
        _lang_name_to_code.put("Pohnpeian", "pon");
        _lang_name_to_code.put("Portuguese", "pt");
        _lang_name_to_code.put("Prakrit languages", "pra");
        _lang_name_to_code.put("ProvenC'al, Old (to 1500); Occitan, Old (to 1500)", "pro");
        _lang_name_to_code.put("Pushto; Pashto", "ps");
        _lang_name_to_code.put("Quechua", "qu");
        _lang_name_to_code.put("Rajasthani", "raj");
        _lang_name_to_code.put("Rapanui", "rap");
        _lang_name_to_code.put("Rarotongan; Cook Islands Maori", "rar");
        _lang_name_to_code.put("Romance languages", "roa");
        _lang_name_to_code.put("Romansh", "rm");
        _lang_name_to_code.put("Romany", "rom");
        _lang_name_to_code.put("Romanian", "ro");
        _lang_name_to_code.put("Rundi", "rn");
        _lang_name_to_code.put("Aromanian; Arumanian; Macedo-Romanian", "rup");
        _lang_name_to_code.put("Russian", "ru");
        _lang_name_to_code.put("Sandawe", "sad");
        _lang_name_to_code.put("Sango", "sg");
        _lang_name_to_code.put("Yakut", "sah");
        _lang_name_to_code.put("South American Indian languages", "sai");
        _lang_name_to_code.put("Salishan languages", "sal");
        _lang_name_to_code.put("Samaritan Aramaic", "sam");
        _lang_name_to_code.put("Sanskrit", "sa");
        _lang_name_to_code.put("Sasak", "sas");
        _lang_name_to_code.put("Santali", "sat");
        _lang_name_to_code.put("Sicilian", "scn");
        _lang_name_to_code.put("Scots", "sco");
        _lang_name_to_code.put("Selkup", "sel");
        _lang_name_to_code.put("Semitic languages", "sem");
        _lang_name_to_code.put("Irish, Old (to 900)", "sga");
        _lang_name_to_code.put("Sign Languages", "sgn");
        _lang_name_to_code.put("Shan", "shn");
        _lang_name_to_code.put("Sidamo", "sid");
        _lang_name_to_code.put("Sinhala; Sinhalese", "si");
        _lang_name_to_code.put("Siouan languages", "sio");
        _lang_name_to_code.put("Sino-Tibetan languages", "sit");
        _lang_name_to_code.put("Slavic languages", "sla");
        _lang_name_to_code.put("Slovak", "sk");
        _lang_name_to_code.put("Slovenian", "sl");
        _lang_name_to_code.put("Southern Sami", "sma");
        _lang_name_to_code.put("Northern Sami", "se");
        _lang_name_to_code.put("Sami languages", "smi");
        _lang_name_to_code.put("Lule Sami", "smj");
        _lang_name_to_code.put("Inari Sami", "smn");
        _lang_name_to_code.put("Samoan", "sm");
        _lang_name_to_code.put("Skolt Sami", "sms");
        _lang_name_to_code.put("Shona", "sn");
        _lang_name_to_code.put("Sindhi", "sd");
        _lang_name_to_code.put("Soninke", "snk");
        _lang_name_to_code.put("Sogdian", "sog");
        _lang_name_to_code.put("Somali", "so");
        _lang_name_to_code.put("Songhai languages", "son");
        _lang_name_to_code.put("Sotho, Southern", "st");
        _lang_name_to_code.put("Spanish; Castilian", "es");
        _lang_name_to_code.put("Sardinian", "sc");
        _lang_name_to_code.put("Sranan Tongo", "srn");
        _lang_name_to_code.put("Serbian", "sr");
        _lang_name_to_code.put("Serer", "srr");
        _lang_name_to_code.put("Nilo-Saharan languages", "ssa");
        _lang_name_to_code.put("Swati", "ss");
        _lang_name_to_code.put("Sukuma", "suk");
        _lang_name_to_code.put("Sundanese", "su");
        _lang_name_to_code.put("Susu", "sus");
        _lang_name_to_code.put("Sumerian", "sux");
        _lang_name_to_code.put("Swahili", "sw");
        _lang_name_to_code.put("Swedish", "sv");
        _lang_name_to_code.put("Classical Syriac", "syc");
        _lang_name_to_code.put("Syriac", "syr");
        _lang_name_to_code.put("Tahitian", "ty");
        _lang_name_to_code.put("Tai languages", "tai");
        _lang_name_to_code.put("Tamil", "ta");
        _lang_name_to_code.put("Tatar", "tt");
        _lang_name_to_code.put("Telugu", "te");
        _lang_name_to_code.put("Timne", "tem");
        _lang_name_to_code.put("Tereno", "ter");
        _lang_name_to_code.put("Tetum", "tet");
        _lang_name_to_code.put("Tajik", "tg");
        _lang_name_to_code.put("Tagalog", "tl");
        _lang_name_to_code.put("Thai", "th");
        _lang_name_to_code.put("Tibetan", "bo");
        _lang_name_to_code.put("Tigre", "tig");
        _lang_name_to_code.put("Tigrinya", "ti");
        _lang_name_to_code.put("Tiv", "tiv");
        _lang_name_to_code.put("Tokelau", "tkl");
        _lang_name_to_code.put("Klingon; tlhIngan-Hol", "tlh");
        _lang_name_to_code.put("Tlingit", "tli");
        _lang_name_to_code.put("Tamashek", "tmh");
        _lang_name_to_code.put("Tonga (Nyasa)", "tog");
        _lang_name_to_code.put("Tonga (Tonga Islands)", "to");
        _lang_name_to_code.put("Tok Pisin", "tpi");
        _lang_name_to_code.put("Tsimshian", "tsi");
        _lang_name_to_code.put("Tswana", "tn");
        _lang_name_to_code.put("Tsonga", "ts");
        _lang_name_to_code.put("Turkmen", "tk");
        _lang_name_to_code.put("Tumbuka", "tum");
        _lang_name_to_code.put("Tupi languages", "tup");
        _lang_name_to_code.put("Turkish", "tr");
        _lang_name_to_code.put("Altaic languages", "tut");
        _lang_name_to_code.put("Tuvalu", "tvl");
        _lang_name_to_code.put("Twi", "tw");
        _lang_name_to_code.put("Tuvinian", "tyv");
        _lang_name_to_code.put("Udmurt", "udm");
        _lang_name_to_code.put("Ugaritic", "uga");
        _lang_name_to_code.put("Uighur; Uyghur", "ug");
        _lang_name_to_code.put("Ukrainian", "uk");
        _lang_name_to_code.put("Umbundu", "umb");
        _lang_name_to_code.put("Undetermined", "und");
        _lang_name_to_code.put("Urdu", "ur");
        _lang_name_to_code.put("Uzbek", "uz");
        _lang_name_to_code.put("Vai", "vai");
        _lang_name_to_code.put("Venda", "ve");
        _lang_name_to_code.put("Vietnamese", "vi");
        _lang_name_to_code.put("VolapC<k", "vo");
        _lang_name_to_code.put("Votic", "vot");
        _lang_name_to_code.put("Wakashan languages", "wak");
        _lang_name_to_code.put("Wolaitta; Wolaytta", "wal");
        _lang_name_to_code.put("Waray", "war");
        _lang_name_to_code.put("Washo", "was");
        _lang_name_to_code.put("Welsh", "cy");
        _lang_name_to_code.put("Sorbian languages", "wen");
        _lang_name_to_code.put("Walloon", "wa");
        _lang_name_to_code.put("Wolof", "wo");
        _lang_name_to_code.put("Kalmyk; Oirat", "xal");
        _lang_name_to_code.put("Xhosa", "xh");
        _lang_name_to_code.put("Yao", "yao");
        _lang_name_to_code.put("Yapese", "yap");
        _lang_name_to_code.put("Yiddish", "yi");
        _lang_name_to_code.put("Yoruba", "yo");
        _lang_name_to_code.put("Yupik languages", "ypk");
        _lang_name_to_code.put("Zapotec", "zap");
        _lang_name_to_code.put("Blissymbols; Blissymbolics; Bliss", "zbl");
        _lang_name_to_code.put("Zenaga", "zen");
        _lang_name_to_code.put("Zhuang; Chuang", "za");
        _lang_name_to_code.put("Zande languages", "znd");
        _lang_name_to_code.put("Zulu", "zu");
        _lang_name_to_code.put("Zuni", "zun");
        _lang_name_to_code.put("Zaza; Dimili; Dimli; Kirdki; Kirmanjki; Zazaki", "zza");
    }

    public Map<String, String> getLangCodeToName() {
        return _lang_code_to_name;
    }

    public Map<String, String> getLangNameToCode() {
        return _lang_name_to_code;
    }
}
