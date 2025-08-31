package com.nova.poneglyph.util;

import org.springframework.stereotype.Component;
//
//@Component
//public class PhoneUtil {
//
//    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
//    private static final Pattern DIGITS_ONLY = Pattern.compile("\\D");
//
//    // إضافة هذا الثابت لتحديد الطول الأدنى لرقم الهاتف
//    public static final int MIN_PHONE_LENGTH = 5;
//
//    public boolean isValidPhoneNumber(String phoneNumber) {
//        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
//    }
//
////    public static String normalizePhone(String phoneNumber) {
////        if (phoneNumber == null) {
////            return null;
////        }
////
////        String digits = DIGITS_ONLY.matcher(phoneNumber).replaceAll("");
////
////        // إزالة الأصفار الزائدة من البداية
////        while (digits.startsWith("0") && digits.length() > 1) {
////            digits = digits.substring(1);
////        }
////
////        return digits;
////    }
//public static String normalizePhone(String phoneNumber, String defaultCountryCode) {
//    if (phoneNumber == null) return null;
//
//    String raw = phoneNumber.trim();
//
//    // إذا كان يتضمن + فالمعالجة تختلف (نحتفظ بالجزء الدولي كما هو)
//    boolean hadPlus = raw.startsWith("+");
//
//    // إزالة كل شيء غير رقمي
//    String digits = DIGITS_ONLY.matcher(raw).replaceAll("");
//
//    // إذا كان يبدأ بـ 00 (بعض الناس يكتبون 00 بدلاً من +)
//    if (digits.startsWith("00")) {
//        digits = digits.substring(2);
//        return digits; // رقم دولي بدون +
//    }
//
//    if (hadPlus) {
//        // لو كان به + في البداية فـ digits الآن يحتوي رمز البلد + باقي الرقم
//        return digits;
//    }
//
//    // الآن الرقم بدون +، بدون أحرف. قد يكون:
//    // - رقم محلي يبدأ بـ 0 (مثال 05xxxx)
//    // - رقم كامل بدون + (مثال 9665xxxx أو 9715xxxx)
//    if (defaultCountryCode != null && !defaultCountryCode.isBlank()) {
//        // إذا كان يبدأ برمز الدولة فعن طريق المصادقة نعيده كما هو
//        if (digits.startsWith(defaultCountryCode)) {
//            return digits;
//        }
//
//        // إذا يبدأ بصفر واحد (trunk) نزيله ونضيف رمز الدولة
//        if (digits.startsWith("0") && digits.length() > 1) {
//            return defaultCountryCode + digits.substring(1);
//        }
//
//        // حالات أرقام قصيرة محلية (مثال: 5xxxxxxxx) — نلصق رمز الدولة
//        // (الشرط الطولي قابل للتعديل حسب بلدك)
//        if (digits.length() <= 10) { // افتراضي: أرقام محلية عادة <=10
//            return defaultCountryCode + digits;
//        }
//    }
//
//    // كحالة افتراضية، نرجع digits كما هي
//    return digits;
//}
//
//    public String formatPhone(String phoneNumber, String countryCode) {
//        if (phoneNumber == null) return null;
//
//        String digits = normalizePhone(phoneNumber);
//
//        // التأكد من أن رقم الهاتف ليس قصيراً جداً
//        if (digits.length() < MIN_PHONE_LENGTH) {
//            throw new IllegalArgumentException("Phone number is too short");
//        }
//
//        // إذا كان رقم الهاتف يبدأ بالفعل برمز الدولة، لا نضيفه مرة أخرى
//        if (!digits.startsWith(countryCode)) {
//            digits = countryCode + digits;
//        }
//
//        return "+" + digits;
//    }
//
//    // دالة جديدة لاستخراج رمز الدولة من رقم هاتف كامل
//    public static String extractCountryCode(String phoneNumber) {
//        if (phoneNumber == null || !phoneNumber.startsWith("+")) {
//            return null;
//        }
//
//        String digits = normalizePhone(phoneNumber);
//
//        // رموز الدول الشائعة وأطوالها
//        if (digits.startsWith("1")) return "1"; // أمريكا الشمالية
//        if (digits.startsWith("20")) return "20"; // مصر
//        if (digits.startsWith("27")) return "27"; // جنوب أفريقيا
//        if (digits.startsWith("30")) return "30"; // اليونان
//        if (digits.startsWith("31")) return "31"; // هولندا
//        if (digits.startsWith("32")) return "32"; // بلجيكا
//        if (digits.startsWith("33")) return "33"; // فرنسا
//        if (digits.startsWith("34")) return "34"; // إسبانيا
//        if (digits.startsWith("36")) return "36"; // المجر
//        if (digits.startsWith("39")) return "39"; // إيطاليا
//        if (digits.startsWith("41")) return "41"; // سويسرا
//        if (digits.startsWith("43")) return "43"; // النمسا
//        if (digits.startsWith("44")) return "44"; // المملكة المتحدة
//        if (digits.startsWith("45")) return "45"; // الدنمارك
//        if (digits.startsWith("46")) return "46"; // السويد
//        if (digits.startsWith("47")) return "47"; // النرويج
//        if (digits.startsWith("48")) return "48"; // بولندا
//        if (digits.startsWith("49")) return "49"; // ألمانيا
//        if (digits.startsWith("51")) return "51"; // بيرو
//        if (digits.startsWith("52")) return "52"; // المكسيك
//        if (digits.startsWith("53")) return "53"; // كوبا
//        if (digits.startsWith("54")) return "54"; // الأرجنتين
//        if (digits.startsWith("55")) return "55"; // البرازيل
//        if (digits.startsWith("56")) return "56"; // تشيلي
//        if (digits.startsWith("57")) return "57"; // كولومبيا
//        if (digits.startsWith("58")) return "58"; // فنزويلا
//        if (digits.startsWith("60")) return "60"; // ماليزيا
//        if (digits.startsWith("61")) return "61"; // أستراليا
//        if (digits.startsWith("62")) return "62"; // إندونيسيا
//        if (digits.startsWith("63")) return "63"; // الفلبين
//        if (digits.startsWith("64")) return "64"; // نيوزيلندا
//        if (digits.startsWith("65")) return "65"; // سنغافورة
//        if (digits.startsWith("66")) return "66"; // تايلاند
//        if (digits.startsWith("81")) return "81"; // اليابان
//        if (digits.startsWith("82")) return "82"; // كوريا الجنوبية
//        if (digits.startsWith("84")) return "84"; // فيتنام
//        if (digits.startsWith("86")) return "86"; // الصين
//        if (digits.startsWith("90")) return "90"; // تركيا
//        if (digits.startsWith("91")) return "91"; // الهند
//        if (digits.startsWith("92")) return "92"; // باكستان
//        if (digits.startsWith("93")) return "93"; // أفغانستان
//        if (digits.startsWith("94")) return "94"; // سريلانكا
//        if (digits.startsWith("95")) return "95"; // ميانمار
//        if (digits.startsWith("98")) return "98"; // إيران
//        if (digits.startsWith("212")) return "212"; // المغرب
//        if (digits.startsWith("213")) return "213"; // الجزائر
//        if (digits.startsWith("216")) return "216"; // تونس
//        if (digits.startsWith("218")) return "218"; // ليبيا
//        if (digits.startsWith("220")) return "220"; // غامبيا
//        if (digits.startsWith("221")) return "221"; // السنغال
//        if (digits.startsWith("222")) return "222"; // موريتانيا
//        if (digits.startsWith("223")) return "223"; // مالي
//        if (digits.startsWith("224")) return "224"; // غينيا
//        if (digits.startsWith("225")) return "225"; // ساحل العاج
//        if (digits.startsWith("226")) return "226"; // بوركينا فاسو
//        if (digits.startsWith("227")) return "227"; // النيجر
//        if (digits.startsWith("228")) return "228"; // توغو
//        if (digits.startsWith("229")) return "229"; // بنين
//        if (digits.startsWith("230")) return "230"; // موريشيوس
//        if (digits.startsWith("231")) return "231"; // ليبيريا
//        if (digits.startsWith("232")) return "232"; // سيراليون
//        if (digits.startsWith("233")) return "233"; // غانا
//        if (digits.startsWith("234")) return "234"; // نيجيريا
//        if (digits.startsWith("235")) return "235"; // تشاد
//        if (digits.startsWith("236")) return "236"; // جمهورية أفريقيا الوسطى
//        if (digits.startsWith("237")) return "237"; // الكاميرون
//        if (digits.startsWith("238")) return "238"; // الرأس الأخضر
//        if (digits.startsWith("239")) return "239"; // ساو تومي وبرينسيبي
//        if (digits.startsWith("240")) return "240"; // غينيا الاستوائية
//        if (digits.startsWith("241")) return "241"; // الغابون
//        if (digits.startsWith("242")) return "242"; // جمهورية الكونغو
//        if (digits.startsWith("243")) return "243"; // جمهورية الكونغو الديمقراطية
//        if (digits.startsWith("244")) return "244"; // أنغولا
//        if (digits.startsWith("245")) return "245"; // غينيا بيساو
//        if (digits.startsWith("246")) return "246"; // إقليم المحيط الهندي البريطاني
//        if (digits.startsWith("247")) return "247"; // جزيرة أسينشين
//        if (digits.startsWith("248")) return "248"; // سيشل
//        if (digits.startsWith("249")) return "249"; // السودان
//        if (digits.startsWith("250")) return "250"; // رواندا
//        if (digits.startsWith("251")) return "251"; // إثيوبيا
//        if (digits.startsWith("252")) return "252"; // الصومال
//        if (digits.startsWith("253")) return "253"; // جيبوتي
//        if (digits.startsWith("254")) return "254"; // كينيا
//        if (digits.startsWith("255")) return "255"; // تنزانيا
//        if (digits.startsWith("256")) return "256"; // أوغندا
//        if (digits.startsWith("257")) return "257"; // بوروندي
//        if (digits.startsWith("258")) return "258"; // موزمبيق
//        if (digits.startsWith("260")) return "260"; // زامبيا
//        if (digits.startsWith("261")) return "261"; // مدغشقر
//        if (digits.startsWith("262")) return "262"; // ريونيون
//        if (digits.startsWith("263")) return "263"; // زيمبابوي
//        if (digits.startsWith("264")) return "264"; // ناميبيا
//        if (digits.startsWith("265")) return "265"; // مالاوي
//        if (digits.startsWith("266")) return "266"; // ليسوتو
//        if (digits.startsWith("267")) return "267"; // بوتسوانا
//        if (digits.startsWith("268")) return "268"; // إسواتيني
//        if (digits.startsWith("269")) return "269"; // جزر القمر
//        if (digits.startsWith("290")) return "290"; // سانت هيلينا
//        if (digits.startsWith("291")) return "291"; // إريتريا
//        if (digits.startsWith("297")) return "297"; // أروبا
//        if (digits.startsWith("298")) return "298"; // جزر فارو
//        if (digits.startsWith("299")) return "299"; // غرينلاند
//        if (digits.startsWith("350")) return "350"; // جبل طارق
//        if (digits.startsWith("351")) return "351"; // البرتغال
//        if (digits.startsWith("352")) return "352"; // لوكسمبورغ
//        if (digits.startsWith("353")) return "353"; // أيرلندا
//        if (digits.startsWith("354")) return "354"; // آيسلندا
//        if (digits.startsWith("355")) return "355"; // ألبانيا
//        if (digits.startsWith("356")) return "356"; // مالطا
//        if (digits.startsWith("357")) return "357"; // قبرص
//        if (digits.startsWith("358")) return "358"; // فنلندا
//        if (digits.startsWith("359")) return "359"; // بلغاريا
//        if (digits.startsWith("370")) return "370"; // ليتوانيا
//        if (digits.startsWith("371")) return "371"; // لاتفيا
//        if (digits.startsWith("372")) return "372"; // إستونيا
//        if (digits.startsWith("373")) return "373"; // مولدوفا
//        if (digits.startsWith("374")) return "374"; // أرمينيا
//        if (digits.startsWith("375")) return "375"; // بيلاروس
//        if (digits.startsWith("376")) return "376"; // أندورا
//        if (digits.startsWith("377")) return "377"; // موناكو
//        if (digits.startsWith("378")) return "378"; // سان مارينو
//        if (digits.startsWith("379")) return "379"; // الفاتيكان
//        if (digits.startsWith("380")) return "380"; // أوكرانيا
//        if (digits.startsWith("381")) return "381"; // صربيا
//        if (digits.startsWith("382")) return "382"; // الجبل الأسود
//        if (digits.startsWith("383")) return "383"; // كوسوفو
//        if (digits.startsWith("385")) return "385"; // كرواتيا
//        if (digits.startsWith("386")) return "386"; // سلوفينيا
//        if (digits.startsWith("387")) return "387"; // البوسنة والهرسك
//        if (digits.startsWith("389")) return "389"; // مقدونيا
//        if (digits.startsWith("420")) return "420"; // التشيك
//        if (digits.startsWith("421")) return "421"; // سلوفاكيا
//        if (digits.startsWith("423")) return "423"; // ليختنشتاين
//        if (digits.startsWith("500")) return "500"; // جزر فوكلاند
//        if (digits.startsWith("501")) return "501"; // بليز
//        if (digits.startsWith("502")) return "502"; // غواتيمالا
//        if (digits.startsWith("503")) return "503"; // السلفادور
//        if (digits.startsWith("504")) return "504"; // هندوراس
//        if (digits.startsWith("505")) return "505"; // نيكاراغوا
//        if (digits.startsWith("506")) return "506"; // كوستاريكا
//        if (digits.startsWith("507")) return "507"; // بنما
//        if (digits.startsWith("508")) return "508"; // سان بيير وميكلون
//        if (digits.startsWith("509")) return "509"; // هايتي
//        if (digits.startsWith("590")) return "590"; // غوادلوب
//        if (digits.startsWith("591")) return "591"; // بوليفيا
//        if (digits.startsWith("592")) return "592"; // غيانا
//        if (digits.startsWith("593")) return "593"; // الإكوادور
//        if (digits.startsWith("594")) return "594"; // غويانا الفرنسية
//        if (digits.startsWith("595")) return "595"; // باراغواي
//        if (digits.startsWith("596")) return "596"; // مارتينيك
//        if (digits.startsWith("597")) return "597"; // سورينام
//        if (digits.startsWith("598")) return "598"; // الأوروغواي
//        if (digits.startsWith("599")) return "599"; // جزر الأنتيل الهولندية
//        if (digits.startsWith("670")) return "670"; // تيمور الشرقية
//        if (digits.startsWith("672")) return "672"; // جزيرة نورفولك
//        if (digits.startsWith("673")) return "673"; // بروناي
//        if (digits.startsWith("674")) return "674"; // ناورو
//        if (digits.startsWith("675")) return "675"; // بابوا غينيا الجديدة
//        if (digits.startsWith("676")) return "676"; // تونغا
//        if (digits.startsWith("677")) return "677"; // جزر سليمان
//        if (digits.startsWith("678")) return "678"; // فانواتو
//        if (digits.startsWith("679")) return "679"; // فيجي
//        if (digits.startsWith("680")) return "680"; // بالاو
//        if (digits.startsWith("681")) return "681"; // واليس وفوتونا
//        if (digits.startsWith("682")) return "682"; // جزر كوك
//        if (digits.startsWith("683")) return "683"; // نييوي
//        if (digits.startsWith("685")) return "685"; // ساموا
//        if (digits.startsWith("686")) return "686"; // كيريباتي
//        if (digits.startsWith("687")) return "687"; // كاليدونيا الجديدة
//        if (digits.startsWith("688")) return "688"; // توفالو
//        if (digits.startsWith("689")) return "689"; // بولينزيا الفرنسية
//        if (digits.startsWith("690")) return "690"; // توكيلاو
//        if (digits.startsWith("691")) return "691"; // ميكرونيسيا
//        if (digits.startsWith("692")) return "692"; // جزر مارشال
//        if (digits.startsWith("850")) return "850"; // كوريا الشمالية
//        if (digits.startsWith("852")) return "852"; // هونغ كونغ
//        if (digits.startsWith("853")) return "853"; // ماكاو
//        if (digits.startsWith("855")) return "855"; // كمبوديا
//        if (digits.startsWith("856")) return "856"; // لاوس
//        if (digits.startsWith("880")) return "880"; // بنغلاديش
//        if (digits.startsWith("886")) return "886"; // تايوان
//        if (digits.startsWith("960")) return "960"; // جزر المالديف
//        if (digits.startsWith("961")) return "961"; // لبنان
//        if (digits.startsWith("962")) return "962"; // الأردن
//        if (digits.startsWith("963")) return "963"; // سوريا
//        if (digits.startsWith("964")) return "964"; // العراق
//        if (digits.startsWith("965")) return "965"; // الكويت
//        if (digits.startsWith("966")) return "966"; // السعودية
//        if (digits.startsWith("967")) return "967"; // اليمن
//        if (digits.startsWith("968")) return "968"; // عمان
//        if (digits.startsWith("970")) return "970"; // فلسطين
//        if (digits.startsWith("971")) return "971"; // الإمارات
//        if (digits.startsWith("972")) return "972"; // إسرائيل
//        if (digits.startsWith("973")) return "973"; // البحرين
//        if (digits.startsWith("974")) return "974"; // قطر
//        if (digits.startsWith("975")) return "975"; // بوتان
//        if (digits.startsWith("976")) return "976"; // منغوليا
//        if (digits.startsWith("977")) return "977"; // نيبال
//        if (digits.startsWith("992")) return "992"; // طاجيكستان
//        if (digits.startsWith("993")) return "993"; // تركمانستان
//        if (digits.startsWith("994")) return "994"; // أذربيجان
//        if (digits.startsWith("995")) return "995"; // جورجيا
//        if (digits.startsWith("996")) return "996"; // قيرغيزستان
//        if (digits.startsWith("998")) return "998"; // أوزبكستان
//
//        // إذا لم يتطابق مع أي من الرموز أعلاه، نعود إلى الإزالة البسيطة للأحرف غير الرقمية
//        return digits;
//    }
//}


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

@Component
public class PhoneUtil {

    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    // أقل طول عملي للجزء الرقمي (بما في ذلك رمز البلد) — عدّل حسب حاجتك
    public static final int MIN_PHONE_LENGTH = 8;

    /**
     * يحاول تحويل أي تمثيل لرقم (مثلاً: +9665..., 009665..., 05..., 5...) إلى E.164.
     * @param phoneRaw رقم الهاتف الوارد من العميل.
     * @param defaultRegion رمز البلد ISO (مثلاً "SA" أو "YE"). يستخدم عندما يكون الرقم محلياً.
     * @return رقم بصيغة E.164 مع علامة + (مثل "+9665....") إذا كان صالحاً، أو null إذا لم يتم التحقق.
     */
//    public static String normalizeToE164(String phoneRaw, String defaultRegion) {
//        if (phoneRaw == null) return null;
//        try {
//            Phonenumber.PhoneNumber pn = phoneUtil.parse(phoneRaw.trim(), defaultRegion);
//            if (!phoneUtil.isPossibleNumber(pn) || !phoneUtil.isValidNumber(pn)) {
//                return null;
//            }
//            return phoneUtil.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164); // يعطي +...
//        } catch (NumberParseException e) {
//            return null;
//        }
//    }

    // مثال للتطبيع لصيغة E.164
    public static String normalizeToE164(String phoneRaw, String defaultRegion) {
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneRaw, defaultRegion);
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * الشكل المخزّن في DB في هذه الاقتراحات: نستخدم E.164 **بدون** علامة + (فقط أرقام).
     * هذه الدالة تقبل نفس المدخلات وتعيد نصاً رقمياً (مثال: "9665...") أو null.
     */
//    public static String normalizeForStorage(String phoneRaw, String defaultRegion) {
//        String e164 = normalizeToE164(phoneRaw, defaultRegion);
//        if (e164 == null) return null;
//        // نزيل علامة زائد
//        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
//        // تحقق طولي بسيط
//        if (digits.length() < MIN_PHONE_LENGTH) return null;
//        return digits;
//    }

    // مثال للتطبيع للتخزين (يحفظ normalized)
    public static String normalizeForStorage(String phone, String region) {
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phone, region);

            return String.valueOf(number.getCountryCode()) + number.getNationalNumber();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * استخراج رمز الدولة من E.164 (يرجع رمز مثل "966" أو null)
     */
    public static String extractCountryCodeFromE164(String e164WithPlus) {
        if (e164WithPlus == null) return null;
        try {
            Phonenumber.PhoneNumber pn = phoneUtil.parse(e164WithPlus, null);
            return String.valueOf(pn.getCountryCode());
        } catch (NumberParseException ex) {
            return null;
        }
    }

    /**
     * محاولة توحيد الرقم بعدة استراتيجيات:
     *  - محاولة عادية (libphonenumber مع defaultRegion)
     *  - محاولة بإضافة رمز الدولة الرقمي (defaultCountryNumeric) عند الحاجة
     *  - محاولة بعد إزالة الـ trunk prefix (صفر البداية)
     *
     * @param phoneRaw المدخل كما جاء من الجهاز (مثل "+9677..." أو "712345678" أو "0712345678")
     * @param defaultRegion رمز الـ ISO الافتراضي (مثلاً "YE")
     * @param defaultCountryNumeric رمز الدولة الرقمي الافتراضي (مثلاً "967")
     * @return normalized digits (E.164 بدون +) أو null إذا فشل التوحيد
     */
    public static String normalizeWithFallbacks(String phoneRaw, String defaultRegion, String defaultCountryNumeric) {
        if (phoneRaw == null) return null;

        // محاولة بسيطة أولاً (libphonenumber العادي)
        String first = normalizeForStorage(phoneRaw, defaultRegion);
        if (first != null) return first;

        // نحصل على الأرقام فقط
        String digits = phoneRaw.replaceAll("\\D+", "");
        if (digits.isEmpty()) return null;

        // إذا الرقم يبدأ بالفعل برمز الدولة الرقمي (مثلاً "9677...") حاول إضافة + وتجربة
        if (defaultCountryNumeric != null && digits.startsWith(defaultCountryNumeric)) {
            String cand = "+" + digits;
            String candNorm = normalizeForStorage(cand, defaultRegion);
            if (candNorm != null) return candNorm;
        }

        // إذا كان يبدأ بصفر trunk prefix مثل 07... نزيل الصفر ونلصق رمز الدولة
        if (defaultCountryNumeric != null && digits.startsWith("0") && digits.length() > 1) {
            String withoutTrunk = digits.replaceFirst("^0+", "");
            String cand = "+" + defaultCountryNumeric + withoutTrunk;
            String candNorm = normalizeForStorage(cand, defaultRegion);
            if (candNorm != null) return candNorm;
        }

        // محاولة افتراضية: نلصق رمز الدولة (لازالة أي صفر بداية أولاً)
        if (defaultCountryNumeric != null) {
            String cleaned = digits.replaceFirst("^0+", "");
            String cand = "+" + defaultCountryNumeric + cleaned;
            String candNorm = normalizeForStorage(cand, defaultRegion);
            if (candNorm != null) return candNorm;
        }

        // محاولة أخيرة: إذا الرقم قصير نسبياً لكن معقول، جرب إضافة '+' مباشرة وربما يعمل
        if (digits.length() >= 5 && digits.length() <= 15) {
            String cand = "+" + digits;
            String candNorm = normalizeForStorage(cand, defaultRegion);
            if (candNorm != null) return candNorm;
        }

        return null;
    }



    public static String extractRegionFromE164(String e164) {
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(e164, null); // null = نعتمد على "+" فقط
            String region = phoneUtil.getRegionCodeForNumber(number);
            return region != null ? region : "YE"; // fallback if null
        } catch (Exception e) {
            return "YE"; // fallback
        }
    }



}
