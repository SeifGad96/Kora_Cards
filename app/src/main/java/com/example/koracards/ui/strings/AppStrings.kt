package com.example.koracards.ui.strings

/**
 * Centralised string resources for KoraCards supporting Arabic and English.
 *
 * Each property takes [isArabic] and returns the appropriate string.
 * Arabic is the default language (PRD §4.11).
 *
 * Usage:
 *   val s = AppStrings(isArabic)
 *   Text(s.homeTitle)
 */
data class AppStrings(val isArabic: Boolean) {

    // ─── Generic ───────────────────────────────────────────────────────────────
    val back          get() = if (isArabic) "رجوع"           else "Back"
    val next          get() = if (isArabic) "التالي"          else "Next"
    val cancel        get() = if (isArabic) "إلغاء"           else "Cancel"
    val start         get() = if (isArabic) "ابدأ"            else "Start"
    val ready         get() = if (isArabic) "جاهز"            else "Ready"
    val proceed       get() = if (isArabic) "متابعة"          else "Proceed"
    val retry         get() = if (isArabic) "إعادة المحاولة"   else "Retry"
    val host          get() = if (isArabic) "المضيف"          else "Host"
    val guest         get() = if (isArabic) "الضيف"           else "Guest"

    // ─── Splash ────────────────────────────────────────────────────────────────
    val splashSubtitle  get() = if (isArabic) "لعبة تخمين كرة القدم" else "Football Guessing Game"
    val splashEnterBtn  get() = if (isArabic) "ادخل اللعبة"           else "Enter Game"

    // ─── Home ──────────────────────────────────────────────────────────────────
    val homeTitle       get() = if (isArabic) "أهلاً بك في كورا كاردز" else "Welcome to KoraCards"
    val homeHostBtn     get() = if (isArabic) "استضافة لعبة"           else "Host Game"
    val homeJoinBtn     get() = if (isArabic) "الانضمام للعبة"          else "Join Game"
    val homeSettingsBtn get() = if (isArabic) "الإعدادات"               else "Settings"
    val hotspotHintHost get() = if (isArabic) "📶 كمضيف: فعّل نقطة الاتصال (Hotspot) أولاً" else "📶 As host: Enable Wi-Fi hotspot on your phone first"
    val hotspotHintGuest get() = if (isArabic) "📱 كضيف: اتصل بشبكة المضيف أولاً" else "📱 As guest: Connect to host's Wi-Fi hotspot first"

    // ─── Settings ──────────────────────────────────────────────────────────────
    val settingsTitle         get() = if (isArabic) "الإعدادات"             else "Settings"
    val settingsCurrentLang   get() = if (isArabic) "اللغة الحالية: العربية" else "Current Language: English"
    val settingsToggleBtn     get() = if (isArabic) "Switch to English"      else "التغيير للعربية"

    // ─── Host Waiting (Lobby) ──────────────────────────────────────────────────
    val lobbyTitle        get() = if (isArabic) "قاعة الانتظار"                  else "Waiting Lobby"
    val lobbyRoomCode     get() = if (isArabic) "رمز الغرفة"                     else "Room Code"
    val lobbyWaiting      get() = if (isArabic) "في انتظار انضمام اللاعبين..."   else "Waiting for players to join..."
    val lobbyStartBtn     get() = if (isArabic) "بدء المباراة"                   else "Start Match"
    val lobbyLeaveBtn     get() = if (isArabic) "مغادرة القاعة"                  else "Leave Lobby"

    // ─── Join Game ─────────────────────────────────────────────────────────────
    val joinTitle         get() = if (isArabic) "الانضمام لغرفة"              else "Join Room"
    val joinHint          get() = if (isArabic) "أدخل الرمز المؤلف من 4 أرقام" else "Enter the 4-digit code from host"
    val joinConfirmBtn    get() = if (isArabic) "تأكيد الرمز وإدخال الاسم"    else "Confirm Code & Enter Name"

    // ─── Player Name Entry ─────────────────────────────────────────────────────
    val playerNameTitle   get() = if (isArabic) "ملف اللاعب"  else "Player Profile"
    val playerNameRoleHost get() = if (isArabic) "مضيف"        else "Host"
    val playerNameRoleGuest get() = if (isArabic) "ضيف"        else "Guest"
    val playerNameRoleLabel get() = if (isArabic) "الدور"      else "Role"
    val playerNameHint    get() = if (isArabic) "اسمك في اللعبة" else "Your in-game name"

    // ─── Game Config ───────────────────────────────────────────────────────────
    val gameConfigTitle   get() = if (isArabic) "إعداد اللعبة"                      else "Game Configuration"
    val gameConfigHostLabel get() = if (isArabic) "المضيف"                           else "Host"
    val gameConfigHint    get() = if (isArabic) "حدد عدد الجولات والنقاط لكل بطاقة" else "Set rounds, points per card, etc."
    val gameConfigStartBtn get() = if (isArabic) "إنشاء غرفة والانتظار"              else "Create Room & Wait"

    // ─── Round Start ───────────────────────────────────────────────────────────
    val roundStartLabel   get() = if (isArabic) "الجولة"              else "Round"
    val roundStartGetReady get() = if (isArabic) "استعد للعب"          else "Get ready to play"
    val roundStartMyTurn  get() = if (isArabic) "دورك للوصف"          else "Your Turn to Describe"
    val roundStartOpponent get() = if (isArabic) "المنافس يصف الآن"   else "Opponent is Describing"
    val roundStartBtn     get() = if (isArabic) "ابدأ"                else "Start"

    // ─── Gameplay — My Card (I'm describing) ──────────────────────────────────
    val myCardMyTurnLabel  get() = if (isArabic) "دورك: صف هذه البطاقة" else "Your Turn: Describe this card"
    val myCardOpenMcqBtn   get() = if (isArabic) "فتح الاختيار المتعدد"  else "Open MCQ"
    val myCardRevealBtn    get() = if (isArabic) "اكشف البطاقة"          else "Reveal Card"

    // ─── Gameplay — Opponent Guessing ─────────────────────────────────────────
    val opponentLabel       get() = if (isArabic) "المنافس يصف"           else "Opponent Describing"
    val opponentGuessTitle  get() = if (isArabic) "خمّن اللاعب!"          else "Guess the player!"
    val opponentGuessHint   get() = if (isArabic) "استمع لتلميحات منافسك بعناية." else "Listen to your opponent's clues carefully."
    val opponentFaultsLabel get() = if (isArabic) "الأخطاء"               else "Faults"
    val opponentAddFaultBtn get() = if (isArabic) "إضافة خطأ"             else "Add Fault"
    val opponentRevealBtn   get() = if (isArabic) "اكشف البطاقة"          else "Reveal Card"

    // ─── MCQ ──────────────────────────────────────────────────────────────────
    val mcqTitle          get() = if (isArabic) "سؤال الاختيار المتعدد"  else "Multiple Choice Clue"
    val mcqQuestion       get() = if (isArabic) "من هذا اللاعب؟"         else "Which player is this?"

    // ─── Card Reveal ──────────────────────────────────────────────────────────
    val revealTitle         get() = if (isArabic) "الكشف"                  else "REVEAL"
    val revealGuestWon      get() = if (isArabic) "النقطة للضيف!"          else "Point awarded to Guest!"
    val revealShowResults   get() = if (isArabic) "عرض نتائج الجولة"       else "Show Round Results"
    val revealSuddenDeath   get() = if (isArabic) "تفعيل الموت المفاجئ"    else "Sudden Death"

    // ─── Round Result ─────────────────────────────────────────────────────────
    val roundResultTitle    get() = if (isArabic) "انتهت الجولة"           else "Round Finished"
    val roundResultWinner   get() = if (isArabic) "الفائز"                 else "Winner"
    val roundResultNextBtn  get() = if (isArabic) "الجولة التالية"         else "Next Round"
    val roundResultFinishBtn get() = if (isArabic) "إنهاء المباراة"        else "Finish Match"

    // ─── Sudden Death Intro ───────────────────────────────────────────────────
    val suddenDeathTitle    get() = if (isArabic) "الموت المفاجئ"         else "SUDDEN DEATH"
    val suddenDeathSubtitle get() = if (isArabic) "تعادل! من يسجل أولاً يفوز!" else "Tie Game! First to score wins!"

    // ─── Match Winner ─────────────────────────────────────────────────────────
    val matchWinnerChampion  get() = if (isArabic) "البطل"                  else "CHAMPION"
    val matchWinnerMessage   get() = if (isArabic) "فاز بالمباراة!"         else "has won the match!"
    val matchWinnerBackBtn   get() = if (isArabic) "العودة للقائمة الرئيسية" else "Back to Main Menu"

    // ─── Connection Lost ──────────────────────────────────────────────────────
    val connectionLostTitle  get() = if (isArabic) "انقطع الاتصال"                            else "Connection Lost"
    val connectionLostDesc   get() = if (isArabic) "تحقق من شبكتك وحاول مجدداً."              else "Please check your network and try again."

    // ── Gameplay shared bar ───────────────────────────────────────────────────
    val scoreLabel         get() = if (isArabic) "النقاط" else "Score"
    val timerLabel         get() = if (isArabic) "الوقت" else "Time"
    val faultsLabel        get() = if (isArabic) "أخطاء" else "Faults"
    val yourTurn           get() = if (isArabic) "دورك" else "Your Turn"
    val opponentTurn       get() = if (isArabic) "دور الخصم" else "Opponent's Turn"
    val correctBtn         get() = if (isArabic) "صحيح ✓" else "Correct ✓"
    val wrongBtn           get() = if (isArabic) "خطأ ✗" else "Wrong ✗"
    val waitingForOpponent get() = if (isArabic) "في انتظار الخصم..." else "Waiting for opponent..."

    // ── Gameplay — My Card ────────────────────────────────────────────────────
    val myCardTitle      get() = if (isArabic) "بطاقتي السرية" else "My Secret Card"
    val positionLabel    get() = if (isArabic) "المركز" else "Position"
    val teamLabel        get() = if (isArabic) "النادي" else "Club"
    val nationalityLabel get() = if (isArabic) "الجنسية" else "Nationality"
    val leagueLabel      get() = if (isArabic) "الدوري" else "League"
    val footLabel        get() = if (isArabic) "القدم" else "Foot"
    val ageLabel         get() = if (isArabic) "العمر" else "Age"
    val shirtLabel       get() = if (isArabic) "رقم القميص" else "Shirt #"

    // ── MCQ ───────────────────────────────────────────────────────────────────
    val mcqWaiting   get() = if (isArabic) "في انتظار الخصم للإجابة..." else "Waiting for opponent's answer..."
    val mcqSubmitted get() = if (isArabic) "تم إرسال إجابتك" else "Answer submitted!"

    // ── Card Reveal ───────────────────────────────────────────────────────────
    val opponentCard get() = if (isArabic) "بطاقة الخصم" else "Opponent's Card"
    val revealingIn  get() = if (isArabic) "يتم الكشف..." else "Revealing..."

    // ── Round Result ──────────────────────────────────────────────────────────
    val guestWaiting get() = if (isArabic) "في انتظار المضيف..." else "Waiting for host..."

    // ── Game Config ───────────────────────────────────────────────────────────
    val configRoundsToWin  get() = if (isArabic) "جولات للفوز" else "Rounds to Win"
    val configMaxFaults    get() = if (isArabic) "أقصى عدد للأخطاء" else "Max Faults"
    val configTimerLabel   get() = if (isArabic) "مؤقت الدور" else "Turn Timer"
    val configTimerOff     get() = if (isArabic) "إيقاف" else "Off"
    val configTimerSeconds get() = if (isArabic) "ثانية" else "sec"
    val configMcqToggle    get() = if (isArabic) "تفعيل الاختيار المتعدد" else "Enable MCQ"
    val configMcqThreshold get() = if (isArabic) "سؤال قبل الاختيار المتعدد" else "Questions before MCQ"
    val configStartGame    get() = if (isArabic) "ابدأ المباراة" else "Start Game"

    // ── Player Name Entry ─────────────────────────────────────────────────────
    val playerNameEnterYours   get() = if (isArabic) "أدخل اسمك" else "Enter your name"
    val playerNameGuestWaiting get() = if (isArabic) "في انتظار الضيف..." else "Guest is entering name..."
    val playerNameHostWaiting  get() = if (isArabic) "في انتظار المضيف..." else "Host is entering name..."
    val playerNameConfirm      get() = if (isArabic) "تأكيد" else "Confirm"

    // ── Host Waiting ──────────────────────────────────────────────────────────
    val lobbyGuestConnected get() = if (isArabic) "اتصل الضيف! انتظر..." else "Guest connected! Waiting..."
    val lobbyConnecting     get() = if (isArabic) "جارٍ البحث..." else "Searching..."

    // ── Join Game ─────────────────────────────────────────────────────────────
    val joinConnecting    get() = if (isArabic) "جارٍ الاتصال..." else "Connecting..."
    val joinRoomCodeLabel get() = if (isArabic) "الرمز" else "Room Code"
    val joinInvalidCode   get() = if (isArabic) "رمز غير صالح (4 أحرف)" else "Invalid code (4 chars needed)"

    // ── Connection Lost ───────────────────────────────────────────────────────
    val connectionLostCountdown get() = if (isArabic) "إعادة المحاولة خلال:" else "Retrying in:"
    val connectionLostGoHome    get() = if (isArabic) "العودة للرئيسية" else "Go Home"

    // ── Splash ────────────────────────────────────────────────────────────────
    val splashTagline get() = if (isArabic) "خمّن اللاعب، افوز بالمباراة!" else "Guess the player, win the match!"

    // ── Settings About section ────────────────────────────────────────────────
    val settingsAboutTitle get() = if (isArabic) "عن التطبيق" else "About"
    val settingsVersion    get() = if (isArabic) "الإصدار 1.0" else "Version 1.0"
    val settingsTagline    get() = if (isArabic) "لعبة تخمين كرة القدم للاعبين" else "Football guessing game for two"

    // ── Match Winner ──────────────────────────────────────────────────────────
    val matchPlayAgainBtn get() = if (isArabic) "العب مجدداً" else "Play Again"

    // ── Sudden Death ──────────────────────────────────────────────────────────
    val suddenDeathNoFaults get() = if (isArabic) "لا حد للأخطاء! أول من يصيب يفوز!" else "No fault limit! First to guess correctly wins!"
}
