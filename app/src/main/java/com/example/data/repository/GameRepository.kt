package com.example.data.repository

import com.example.data.model.GameCategory
import com.example.data.model.GameQuestion
import com.example.data.model.MatchPair
import com.example.data.model.TimelineItem

object GameRepository {
    val historyTimeline = listOf(
        TimelineItem(1336, "1336-yil", "Amir Temur tavalludi", "Kesh yaqinidagi Xo'ja Ilg'or qishlog'ida sohibqiron Amir Temur tavallud topdi."),
        TimelineItem(1370, "1370-yil", "Temuriylar davlatiga asos solinishi", "Amir Temur butun Movarounnahr hukmdori deb e'lon qilindi va Samarqand poytaxt etib belgilandi."),
        TimelineItem(1444, "1444-yil", "Mirzo Ulug'bek 'Ziji Ko'ragoniy' asarini yakunladi", "1018 ta yulduzning aniq koordinatalarini o'z ichiga olgan fundamental astronomik jadval."),
        TimelineItem(1483, "1483-yil", "Zahiriddin Muhammad Bobur tavalludi", "Andijonda Boburiylar saltanati asoschisi va 'Boburnoma' muallifi tug'ildi."),
        TimelineItem(1485, "1485-yil", "Alisher Navoiy 'Xamsa'ni yakunladi", "Turkiy tildagi birinchi besh doston (Hayrat ul-abror, Farhod va Shirin, Layli va Majnun, Sab'ai sayyor, Saddi Iskandariy)."),
        TimelineItem(1991, "1991-yil", "O'zbekiston Respublikasi Mustaqilligi e'lon qilindi", "31-avgust kuni O'zbekiston o'z davlat mustaqilligini tantanali e'lon qildi.")
    )

    val gameQuestions = listOf(
        GameQuestion("g_hist_1", GameCategory.HISTORY, "Sohibqiron Amir Temurning davlat muhridagi mashhur shiori qaysi?", listOf("Kuch — adolatdadir", "Ilm — najotdir", "Birlikda baraka", "Adolat — qonunda"), 0, "Amir Temurning davlat tamg'asida 'Rosti-rusti' (Kuch — adolatdadir) so'zlari bitilgan edi.", 20),
        GameQuestion("g_hist_2", GameCategory.HISTORY, "Samarqanddagi mashhur Registon maydonida qaysi uch madrasa joylashgan?", listOf("Ulug'bek, Sherdor, Tillakori", "Ko'kaldosh, Mir Arab, Ulug'bek", "Baroqxon, Nodir Devonbegi, Sherdor", "Xoja Ahror, Tillakori, Ulug'bek"), 0, "Registon ansambli: Ulug'bek (1417-1420), Sherdor (1619-1636) va Tillakori (1646-1660) madrasalaridan iborat.", 20),
        GameQuestion("g_geo_1", GameCategory.GEOGRAPHY, "O'zbekiston Respublikasining maydoni qancha?", listOf("448.9 ming km²", "550.0 ming km²", "380.5 ming km²", "620.1 ming km²"), 0, "O'zbekiston maydoni 448,978 kvadrat kilometrni tashkil etadi.", 20),
        GameQuestion("g_geo_2", GameCategory.GEOGRAPHY, "Dunyoning eng baland cho'qqisi — Everest (Jomolungma) qaysi tog' tizmasida joylashgan?", listOf("Himoloy", "And", "Kordilyera", "Tyan-Shan"), 0, "Everest (8848.86 m) Nepal va Xitoy chegarasidagi Himoloy tog'larida joylashgan.", 20),
        GameQuestion("g_geo_3", GameCategory.GEOGRAPHY, "Yaponiya poytaxti qaysi shahar?", listOf("Tokio", "Kioto", "Osaka", "Seul"), 0, "Tokio — Yaponiyaning poytaxti va dunyodagi eng yirik aglomeratsiyalardan biridir.", 20),
        GameQuestion("g_math_1", GameCategory.MATH, "Tezkor hisob: 15 × 16 = ?", listOf("240", "225", "256", "230"), 0, "15 × 16 = 15 × (15 + 1) = 225 + 15 = 240.", 20),
        GameQuestion("g_math_2", GameCategory.MATH, "Ketma-ketlikdagi keyingi sonni toping: 2, 6, 12, 20, 30, ?", listOf("42", "40", "36", "48"), 0, "Qadamlar: +4, +6, +8, +10, keyingisi +12, demak 30 + 12 = 42.", 20),
        GameQuestion("g_math_3", GameCategory.MATH, "To'g'ri burchakli uchburchakning katetlari 6 va 8 bo'lsa, gipotenuzasi qancha?", listOf("10", "12", "14", "16"), 0, "Pifagor bo'yicha: √(6² + 8²) = √(36 + 64) = √100 = 10.", 20),
        GameQuestion("g_lit_1", GameCategory.UZBEK_LIT, "O'zbek romanchiligining asoschisi kim?", listOf("Abdulla Qodiriy", "Abdulhamid Cho'lpon", "Oybek", "G'afur G'ulom"), 0, "Abdulla Qodiriy 'O'tkan kunlar' (1922-1926) asari bilan o'zbek milliy romanchiligiga asos solgan.", 20),
        GameQuestion("g_lit_2", GameCategory.UZBEK_LIT, "Qaysi so'z imlo jihatdan to'g'ri yozilgan?", listOf("Taqriz", "Taqris", "Tag'riz", "Taxriz"), 0, "Imlo lug'atiga ko'ra 'Taqriz' so'zi oxirida 'z' harfi bilan yoziladi.", 20),
        GameQuestion("g_lit_3", GameCategory.UZBEK_LIT, "Maqolni to'ldiring: 'Ilm baxt keltirar, bilim — ...'", listOf("taxt keltirar", "do'st orttirar", "boylik to'plar", "ziyo sochar"), 0, "'Ilm baxt keltirar, bilim — taxt keltirar' xalq donishmandligi maqoli.", 20),
        GameQuestion("g_eng_1", GameCategory.ENGLISH, "'Perseverance' so'zining o'zbekcha ma'nosi qaysi?", listOf("Matonat, sabot-matonat", "Dangasalik", "Kamtarlik", "Saxovat"), 0, "'Perseverance' — qiyinchiliklarga qaramay maqsad sari intilish, sabot-matonat demakdir.", 20),
        GameQuestion("g_eng_2", GameCategory.ENGLISH, "To'g'ri grammatik shaklni tanlang: 'She _____ studying English for 5 years.'", listOf("has been", "is been", "have been", "was been"), 0, "Present Perfect Continuous shaklida uchinchi shaxs uchun 'has been' ishlatiladi.", 20),
        GameQuestion("g_sci_1", GameCategory.SCIENCE, "Oltin elementining kimyoviy belgisi qanday?", listOf("Au", "Ag", "Fe", "Cu"), 0, "Oltinning lotincha nomi Aurum, belgisi Au (atom raqami 79).", 20),
        GameQuestion("g_sci_2", GameCategory.SCIENCE, "Fizikada quvvatning Xalqaro birliklar sistemasidagi (SI) birligi nima?", listOf("Vatt (W)", "Joul (J)", "Paskal (Pa)", "Nyuton (N)"), 0, "Quvvat birligi — Vatt (Joul / sekund).", 20),
        GameQuestion("g_cs_1", GameCategory.INFORMATICS, "Murakkab parol yaratishda qaysi tamoyil eng xavfsiz hisoblanadi?", listOf("Katta-kichik harflar, sonlar va maxsus belgilar aralashmasi (kamida 12 belgi)", "Faqat o'z tug'ilgan yili va ismi", "12345678 ketma-ketligi", "Oddiy lug'at so'zi"), 0, "Kuchli parol kombinatsiyasida turli registrdagi harflar, raqamlar va ramzlar bo'lishi shart.", 20),
        GameQuestion("g_cs_2", GameCategory.INFORMATICS, "Quyidagilardan qaysi biri ma'lumotlar bazasini boshqarish tizimi (MBBT) hisoblanadi?", listOf("PostgreSQL", "Photoshop", "Notepad", "Figma"), 0, "PostgreSQL — dunyodagi eng ilg'or ochiq kodli relyatsion ma'lumotlar bazasi tizimidir.", 20)
    )

    val capitalsMatching = listOf(
        MatchPair("O'zbekiston", "Toshkent"),
        MatchPair("Turkiya", "Anqara"),
        MatchPair("Fransiya", "Parij"),
        MatchPair("Germaniya", "Berlin"),
        MatchPair("Buyuk Britaniya", "London"),
        MatchPair("Yaponiya", "Tokio"),
        MatchPair("Janubiy Koreya", "Seul"),
        MatchPair("AQSH", "Vashington")
    )

    val authorsMatching = listOf(
        MatchPair("Alisher Navoiy", "Xamsa"),
        MatchPair("Zahiriddin Muhammad Bobur", "Boburnoma"),
        MatchPair("Abdulla Qodiriy", "O'tkan kunlar"),
        MatchPair("Abdulhamid Cho'lpon", "Kecha va kunduz"),
        MatchPair("Fitrat", "Hind ixtilochilari"),
        MatchPair("Oybek", "Navoiy romani")
    )
}
