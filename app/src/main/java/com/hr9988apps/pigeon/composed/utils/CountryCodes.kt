package com.hr9988apps.pigeon.composed.utils

private val countryCode = hashMapOf(
    Pair("+93", "AF"),
    Pair("+355", "AL"),
    Pair("+213", "DZ"),
    Pair("+376", "AD"),
    Pair("+244", "AO"),
    Pair("+672", "AQ"),
    Pair("+54", "AR"),
    Pair("+374", "AM"),
    Pair("+297", "AW"),
    Pair("+61", "AU"),
    Pair("+43", "AT"),
    Pair("+994", "AZ"),
    Pair("+973", "BH"),
    Pair("+880", "BD"),
    Pair("+375", "BY"),
    Pair("+32", "BE"),
    Pair("+501", "BZ"),
    Pair("+229", "BJ"),
    Pair("+975", "BT"),
    Pair("+591", "BO"),
    Pair("+387", "BA"),
    Pair("+267", "BW"),
    Pair("+55", "BR"),
    Pair("+673", "BN"),
    Pair("+359", "BG"),
    Pair("+226", "BF"),
    Pair("+95", "MM"),
    Pair("+257", "BI"),
    Pair("+855", "KH"),
    Pair("+237", "CM"),
    Pair("+1", "CA"),
    Pair("+238", "CV"),
    Pair("+236", "CF"),
    Pair("+235", "TD"),
    Pair("+56", "CL"),
    Pair("+86", "CN"),
    Pair("+61", "CX"),
    Pair("+61", "CC"),
    Pair("+57", "CO"),
    Pair("+269", "KM"),
    Pair("+242", "CG"),
    Pair("+243", "CD"),
    Pair("+682", "CK"),
    Pair("+506", "CR"),
    Pair("+385", "HR"),
    Pair("+53", "CU"),
    Pair("+357", "CY"),
    Pair("+420", "CZ"),
    Pair("+45", "DK"),
    Pair("+253", "DJ"),
    Pair("+670", "TL"),
    Pair("+593", "EC"),
    Pair("+20", "EG"),
    Pair("+503", "SV"),
    Pair("+240", "GQ"),
    Pair("+291", "ER"),
    Pair("+372", "EE"),
    Pair("+251", "ET"),
    Pair("+500", "FK"),
    Pair("+298", "FO"),
    Pair("+679", "FJ"),
    Pair("+358", "FI"),
    Pair("+33", "FR"),
    Pair("+689", "PF"),
    Pair("+241", "GA"),
    Pair("+220", "GM"),
    Pair("+995", "GE"),
    Pair("+49", "DE"),
    Pair("+233", "GH"),
    Pair("+350", "GI"),
    Pair("+30", "GR"),
    Pair("+299", "GL"),
    Pair("+502", "GT"),
    Pair("+224", "GN"),
    Pair("+245", "GW"),
    Pair("+592", "GY"),
    Pair("+509", "HT"),
    Pair("+504", "HN"),
    Pair("+852", "HK"),
    Pair("+36", "HU"),
    Pair("+91", "IN"),
    Pair("+62", "ID"),
    Pair("+98", "IR"),
    Pair("+964", "IQ"),
    Pair("+353", "IE"),
    Pair("+44", "IM"),
    Pair("+972", "IL"),
    Pair("+39", "IT"),
    Pair("+225", "CI"),
    Pair("+81", "JP"),
    Pair("+962", "JO"),
    Pair("+7", "KZ"),
    Pair("+254", "KE"),
    Pair("+686", "KI"),
    Pair("+965", "KW"),
    Pair("+996", "KG"),
    Pair("+856", "LA"),
    Pair("+371", "LV"),
    Pair("+961", "LB"),
    Pair("+266", "LS"),
    Pair("+231", "LR"),
    Pair("+218", "LY"),
    Pair("+423", "LI"),
    Pair("+370", "LT"),
    Pair("+352", "LU"),
    Pair("+853", "MO"),
    Pair("+389", "MK"),
    Pair("+261", "MG"),
    Pair("+265", "MW"),
    Pair("+60", "MY"),
    Pair("+960", "MV"),
    Pair("+223", "ML"),
    Pair("+356", "MT"),
    Pair("+692", "MH"),
    Pair("+222", "MR"),
    Pair("+230", "MU"),
    Pair("+262", "YT"),
    Pair("+52", "MX"),
    Pair("+691", "FM"),
    Pair("+373", "MD"),
    Pair("+377", "MC"),
    Pair("+976", "MN"),
    Pair("+382", "ME"),
    Pair("+212", "MA"),
    Pair("+258", "MZ"),
    Pair("+264", "NA"),
    Pair("+674", "NR"),
    Pair("+977", "NP"),
    Pair("+31", "NL"),
    Pair("+599", "AN"),
    Pair("+687", "NC"),
    Pair("+64", "NZ"),
    Pair("+505", "NI"),
    Pair("+227", "NE"),
    Pair("+234", "NG"),
    Pair("+683", "NU"),
    Pair("+850", "KP"),
    Pair("+47", "NO"),
    Pair("+968", "OM"),
    Pair("+92", "PK"),
    Pair("+680", "PW"),
    Pair("+507", "PA"),
    Pair("+675", "PG"),
    Pair("+595", "PY"),
    Pair("+51", "PE"),
    Pair("+63", "PH"),
    Pair("+870", "PN"),
    Pair("+48", "PL"),
    Pair("+351", "PT"),
    Pair("+1", "PR"),
    Pair("+974", "QA"),
    Pair("+40", "RO"),
    Pair("+7", "RU"),
    Pair("+250", "RW"),
    Pair("+590", "BL"),
    Pair("+685", "WS"),
    Pair("+378", "SM"),
    Pair("+239", "ST"),
    Pair("+966", "SA"),
    Pair("+221", "SN"),
    Pair("+381", "RS"),
    Pair("+248", "SC"),
    Pair("+232", "SL"),
    Pair("+65", "SG"),
    Pair("+421", "SK"),
    Pair("+386", "SI"),
    Pair("+677", "SB"),
    Pair("+252", "SO"),
    Pair("+27", "ZA"),
    Pair("+82", "KR"),
    Pair("+34", "ES"),
    Pair("+94", "LK"),
    Pair("+290", "SH"),
    Pair("+508", "PM"),
    Pair("+249", "SD"),
    Pair("+597", "SR"),
    Pair("+268", "SZ"),
    Pair("+46", "SE"),
    Pair("+41", "CH"),
    Pair("+963", "SY"),
    Pair("+886", "TW"),
    Pair("+992", "TJ"),
    Pair("+255", "TZ"),
    Pair("+66", "TH"),
    Pair("+228", "TG"),
    Pair("+690", "TK"),
    Pair("+676", "TO"),
    Pair("+216", "TN"),
    Pair("+90", "TR"),
    Pair("+993", "TM"),
    Pair("+688", "TV"),
    Pair("+971", "AE"),
    Pair("+256", "UG"),
    Pair("+44", "GB"),
    Pair("+380", "UA"),
    Pair("+598", "UY"),
    Pair("+1", "US"),
    Pair("+998", "UZ"),
    Pair("+678", "VU"),
    Pair("+39", "VA"),
    Pair("+58", "VE"),
    Pair("+84", "VN"),
    Pair("+681", "WF"),
    Pair("+967", "YE"),
    Pair("+260", "ZM"),
    Pair("+263", "ZW"),
)

fun getCountryFromCode(code: String) : String? {
    return countryCode[code]
}
