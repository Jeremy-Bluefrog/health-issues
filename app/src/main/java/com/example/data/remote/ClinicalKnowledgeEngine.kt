package com.example.data.remote

import com.example.data.model.AiAnalysisResult
import com.example.data.model.DepartmentRecommendation
import com.example.data.model.FollowUpQuestion
import com.example.data.model.InitialSymptomInput
import com.example.data.model.PossibleCause
import com.example.data.model.TriageLevel
import java.util.UUID

object ClinicalKnowledgeEngine {

    fun analyze(
        input: InitialSymptomInput,
        history: List<Pair<String, String>>,
        isFinal: Boolean,
        round: Int
    ): AiAnalysisResult {
        val fullText = buildString {
            append(input.description)
            append(" ")
            append(input.duration)
            append(" ")
            append(input.chronicConditions)
            for ((_, text) in history) {
                append(" ")
                append(text)
            }
        }.lowercase()

        val isAbdominal = fullText.contains("腹") || fullText.contains("胃") || fullText.contains("肚") || fullText.contains("腸") || fullText.contains("盲腸")
        val isHeadache = fullText.contains("頭痛") || fullText.contains("頭暈") || fullText.contains("偏頭痛") || fullText.contains("頭重")
        val isChest = fullText.contains("胸") || fullText.contains("心悸") || fullText.contains("喘") || fullText.contains("心跳")
        val isRespiratory = fullText.contains("咳") || fullText.contains("喉嚨") || fullText.contains("發燒") || fullText.contains("流鼻水") || fullText.contains("感冒")
        val isDizziness = fullText.contains("暈") || fullText.contains("眩暈") || fullText.contains("站不穩")

        return when {
            isChest -> analyzeChest(input, fullText, isFinal, round)
            isAbdominal -> analyzeAbdominal(input, fullText, isFinal, round)
            isHeadache -> analyzeHeadache(input, fullText, isFinal, round)
            isRespiratory -> analyzeRespiratory(input, fullText, isFinal, round)
            isDizziness -> analyzeDizziness(input, fullText, isFinal, round)
            else -> analyzeGeneral(input, fullText, isFinal, round)
        }
    }

    private fun analyzeChest(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val hasSevere = text.contains("壓迫") || text.contains("石頭壓") || text.contains("輻射") || text.contains("冷汗") || text.contains("左肩") || input.painLevel >= 8
        val level = if (hasSevere) TriageLevel.EMERGENCY else TriageLevel.PROMPT

        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "chest_q1",
                question = "胸口不適的感覺最接近以下哪一種？",
                purpose = "鑑別缺血性心臟病與肌肉骨骼或神經痛",
                options = listOf("像重物壓在胸口悶痛", "尖銳刺痛或隨呼吸加劇", "火燒心或喉嚨酸水湧上", "突發劇烈撕裂感")
            ),
            FollowUpQuestion(
                id = "chest_q2",
                question = "疼痛或不舒服時，是否有放射到其他部位？",
                purpose = "評估冠狀動脈症候群之放射痛特徵",
                options = listOf("延伸到下巴、左肩膀或左手臂", "延伸到後背肩胛骨間", "局限在胸骨單一點", "無放射痛")
            )
        )

        return AiAnalysisResult(
            triageLevel = level,
            summary = "胸部不適涉及心臟與肺部等重要器官，需保持高度警覺。初步分析可能涉及缺血性心絞痛、胃食道逆流或胸壁肌肉拉傷。",
            possibleCauses = listOf(
                PossibleCause(
                    name = "心絞痛 / 冠狀動脈缺血",
                    likelihood = if (hasSevere) "高度可能" else "中度需鑑別",
                    explanation = "心肌供血不足時常表現為胸骨後壓迫感、悶脹，可能伴隨冒冷汗或下巴/手臂反射痛。",
                    keySigns = listOf("壓榨窒息感", "活動時加劇休息緩解", "伴隨冒冷汗")
                ),
                PossibleCause(
                    name = "胃食道逆流 (GERD)",
                    likelihood = "中度可能",
                    explanation = "胃酸刺激食道下端可引起火燒心或胸悶，飯後或平躺時易加劇。",
                    keySigns = listOf("胸骨後灼熱感", "口中泛酸", "空腹或餐後明顯")
                ),
                PossibleCause(
                    name = "胸廓肌筋膜疼痛 / 肋軟骨炎",
                    likelihood = "良性鑑別",
                    explanation = "姿勢不良、過度用力引起，常在深呼吸、咳嗽或按壓時出現明顯刺痛。",
                    keySigns = listOf("特定按壓點劇痛", "隨姿勢變換改變")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("心臟血管內科", "胸悶壓迫感之第一線心電圖與心肌酵素檢查", "儘速"),
                DepartmentRecommendation("急診醫學科", "若持續壓胸超過15分鐘伴隨冷汗呼吸困難，請直接前往", "緊急")
            ),
            redFlagWarnings = listOf(
                "持續性胸部壓迫感、窒息感超過 15 分鐘不緩解",
                "痛感擴散至下巴、頸部、左肩或背部",
                "伴隨呼吸極度急促、大量冒冷汗、臉色蒼白或暈厥"
            ),
            homeCareAdvice = listOf(
                "立即停止一切劇烈活動，採取半坐臥姿放鬆休息",
                "鬆開胸前緊身衣物，保持呼吸道通暢",
                "若過去有心絞痛病史且醫師曾開立舌下硝化甘油片 (NTG)，可依醫囑含服；若未曾開立請勿擅服他人藥物",
                "切勿強忍自行開車，若警訊浮現請立刻撥打 119"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }

    private fun analyzeAbdominal(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val isRightLower = text.contains("右下") || text.contains("盲腸") || text.contains("闌尾")
        val isUpper = text.contains("上腹") || text.contains("胃") || text.contains("火燒")
        val hasSevere = text.contains("劇烈") || text.contains("反彈痛") || text.contains("冒冷汗") || text.contains("硬") || input.painLevel >= 8

        val level = when {
            hasSevere || (isRightLower && text.contains("發燒")) -> TriageLevel.EMERGENCY
            isRightLower || text.contains("持續") -> TriageLevel.PROMPT
            else -> TriageLevel.ROUTINE_CLINIC
        }

        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "abd_q1",
                question = "腹痛的最主要確切位置在哪裡？",
                purpose = "精確定位腹腔器官（右下腹闌尾、右上腹膽囊、上腹胃部、下腹腸道）",
                options = listOf("肚臍周圍轉移至右下腹", "肚臍上方或心窩處", "右上腹部（肋骨下方）", "肚臍周圍或全腹部")
            ),
            FollowUpQuestion(
                id = "abd_q2",
                question = "是否有伴隨以下哪種症狀？",
                purpose = "鑑別感染發炎、消化道出血或機械性阻塞",
                options = listOf("輕微或明顯發燒", "嘔吐且無法進食飲水", "腹瀉或解黑便/血便", "只有疼痛，無其他症狀")
            ),
            FollowUpQuestion(
                id = "abd_q3",
                question = "輕壓肚子放開時，或是走路震動/跳躍時，疼痛是否明顯加劇？",
                purpose = "檢查是否有腹膜刺激徵象 (Rebound tenderness)",
                options = listOf("手放開或跳躍時明顯震痛 (反彈痛)", "只有壓下去會痛，放開還好", "自己摸肚子感覺很柔軟", "不確定")
            )
        )

        return AiAnalysisResult(
            triageLevel = level,
            summary = "您描述了腹部不適。腹痛成因涵蓋急性闌尾炎、胃炎/潰瘍、膽囊炎與急性腸胃炎等，需特別觀察疼痛轉移與發炎跡象。",
            possibleCauses = listOf(
                PossibleCause(
                    name = if (isRightLower) "急性闌尾炎 (盲腸炎)" else "急性腸胃炎",
                    likelihood = if (isRightLower) "高度需排除" else "常見可能",
                    explanation = if (isRightLower) "初期常自肚臍周圍悶痛，數小時後轉移並固定於右下腹，常伴隨食慾不振或低度發燒。"
                    else "由病毒或細菌感染引起，常見絞痛、腹瀉、噁心反胃。",
                    keySigns = listOf("右下腹壓痛與跳躍震痛", "厭食", "低度發燒")
                ),
                PossibleCause(
                    name = "急性胃炎 / 胃十二指腸潰瘍",
                    likelihood = if (isUpper) "高度可能" else "鑑別可能",
                    explanation = "胃酸侵蝕或幽門桿菌發炎，多位於心窩部或上腹部，常有空腹痛或餐後隱痛。",
                    keySigns = listOf("上腹灼熱悶痛", "噯氣", "反胃")
                ),
                PossibleCause(
                    name = "膽囊炎 / 膽結石發作",
                    likelihood = "鑑別可能",
                    explanation = "多於油膩進食後發作，右上腹陣發性劇痛，有時放射至右肩背部。",
                    keySigns = listOf("右上腹深壓劇痛", "常在油膩餐後", "發燒")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("肝膽腸胃內科 / 一般外科", "進行腹部超音波、觸診與血液發炎指數檢測", if (level == TriageLevel.EMERGENCY) "急診" else "今日/明日就診"),
                DepartmentRecommendation("急診醫學科", "若出現反彈痛、板狀腹或高燒劇痛，應立即就醫", "緊急")
            ),
            redFlagWarnings = listOf(
                "肚子觸摸感覺僵硬如木板（板狀腹）或輕放開即劇烈反彈痛",
                "發高燒（大於 38.5°C）且伴隨畏寒發抖",
                "吐出深咖啡色胃液、吐血或排出瀝青般黑便/鮮血便",
                "劇烈劇痛到無法挺直身軀或伴隨暈眩休克感"
            ),
            homeCareAdvice = listOf(
                "在醫師尚未明確診斷腹痛原因前，【切勿】自行服用強力止痛藥（例如 NSAIDs），以免掩蓋急性闌尾炎或腹膜炎的穿孔惡化徵兆",
                "避免進食油膩、辛辣刺激食物；若持續噁心嘔吐，暫時禁食空腹讓腸胃休息",
                "可採屈膝側臥姿勢，減輕腹部肌肉張力與拉扯",
                "注意體溫變化並隨時觀察疼痛範圍是否擴大"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }

    private fun analyzeHeadache(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val isThunderclap = text.contains("爆炸") || text.contains("雷擊") || text.contains("一生中最痛")
        val hasNeuro = text.contains("手腳無力") || text.contains("嘴歪") || text.contains("說話不清") || text.contains("視力模糊") || text.contains("發抖")

        val level = when {
            isThunderclap || hasNeuro || input.painLevel >= 9 -> TriageLevel.EMERGENCY
            text.contains("劇烈") || text.contains("嘔吐") || input.painLevel >= 7 -> TriageLevel.PROMPT
            else -> TriageLevel.ROUTINE_CLINIC
        }

        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "head_q1",
                question = "這個頭痛發作的速度有多快？",
                purpose = "鑑別蜘蛛膜下腔出血(SAH)之雷擊性頭痛與一般原發性頭痛",
                options = listOf("幾秒鐘內瞬間達到最劇烈 (如雷擊爆炸)", "數十分鐘到幾小時內漸漸加劇", "每天持續好幾天都差不多悶痛", "起床時特別痛")
            ),
            FollowUpQuestion(
                id = "head_q2",
                question = "頭痛時是否有伴隨以下感覺？",
                purpose = "鑑別偏頭痛、緊縮型頭痛或顱內壓增高",
                options = listOf("單側如脈搏般跳痛，怕光怕吵噁心", "整顆頭像被緊箍咒勒住的緊繃鈍痛", "伴隨脖子僵硬且無法向下彎觸及胸口", "無特殊伴隨感覺")
            ),
            FollowUpQuestion(
                id = "head_q3",
                question = "是否有單側手腳麻木無力、嘴角歪斜或說話口齒不清？",
                purpose = "中風與腦神經急性損傷神經學評估 (FAST原則)",
                options = listOf("完全沒有手腳麻木或言語異常", "有輕微頭暈，但四肢活動正常", "有感覺單側無力或手拿不住東西 (警告)", "偶爾眼皮跳動")
            )
        )

        return AiAnalysisResult(
            triageLevel = level,
            summary = "您提到了頭痛問題。頭痛多數為良性原發性頭痛（如緊縮型頭痛、偏頭痛），但必須優先排除急性腦血管意外與顱內病變。",
            possibleCauses = listOf(
                PossibleCause(
                    name = "偏頭痛 (Migraine)",
                    likelihood = "高度常見",
                    explanation = "腦部神經與血管調控敏感所致，典型為單側搏動性疼痛，常伴隨噁心、畏光、怕吵，活動時易加重。",
                    keySigns = listOf("脈搏跳動感", "畏光怕噪音", "噁心")
                ),
                PossibleCause(
                    name = "緊縮型頭痛 (Tension-type Headache)",
                    likelihood = "高度常見",
                    explanation = "與壓力、肩頸肌肉緊繃、睡眠不足密切相關，表現為頭部雙側帶狀緊箍鈍痛。",
                    keySigns = listOf("如緊箍咒壓迫感", "後頸肩膀僵硬", "通常無畏光噁心")
                ),
                PossibleCause(
                    name = "急性顱內病變 / 腦血管問題",
                    likelihood = if (isThunderclap || hasNeuro) "急需排除" else "低度警示",
                    explanation = "若出現突發雷擊劇痛或伴隨神經學缺損，需由腦神經科進行電腦斷層排查。",
                    keySigns = listOf("突發劇烈爆痛", "肢體無力麻木", "脖子僵硬")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("神經內科 (腦神經科)", "專科評估原發性頭痛治療或進一步腦波/影像安排", "門診預約"),
                DepartmentRecommendation("急診醫學科", "若出現雷擊頭痛、劇烈嘔吐或手腳無力，請立刻送醫", "緊急")
            ),
            redFlagWarnings = listOf(
                "突發一生中從未有過的劇烈爆炸性頭痛（雷擊性頭痛）",
                "伴隨單側肢體麻木、手腳無力、嘴角歪斜、言語不清",
                "頸部極度僵硬合併高燒、意識混亂或噴射狀嘔吐",
                "50歲以上首次出現新發生的嚴重頭痛"
            ),
            homeCareAdvice = listOf(
                "在安靜、遮光的陰涼房間內平躺閉目休息",
                "可使用冷敷袋敷於額頭或後頸部以利舒緩搏動性血管充血",
                "補充適度水分，避免咖啡因驟停或過量",
                "記錄頭痛日記（發作時間、誘發因子如睡眠/飲食），以利門診醫師評估"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }

    private fun analyzeRespiratory(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val hasSevereDyspnea = text.contains("喘不過氣") || text.contains("呼吸困難") || text.contains("發紫") || text.contains("窒息")
        val level = if (hasSevereDyspnea) TriageLevel.EMERGENCY else if (text.contains("高燒") || input.painLevel >= 7) TriageLevel.PROMPT else TriageLevel.SELF_CARE

        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "resp_q1",
                question = "目前是否有量測體溫？發燒狀況如何？",
                purpose = "評估感染急性發炎嚴重度",
                options = listOf("體溫超過 38.5°C 且持續畏寒", "低度發燒 (37.5 ~ 38.4°C)", "沒有發燒，僅有局部呼吸道症狀", "退燒藥後又反覆燒起來")
            ),
            FollowUpQuestion(
                id = "resp_q2",
                question = "咳嗽時有痰嗎？呼吸時是否感覺困難或有聲音？",
                purpose = "鑑別上呼吸道感染與支氣管/肺炎/氣喘發作",
                options = listOf("乾咳無痰，喉嚨乾燥發癢", "有大量黃綠色濃痰", "咳嗽劇烈且呼吸時胸口有喘鳴哮聲", "偶爾咳，痰呈透明白色")
            )
        )

        return AiAnalysisResult(
            triageLevel = level,
            summary = "您提到了呼吸道相關不適。多數為病毒性急性上呼吸道感染（如普通感冒、流感、新冠），需注意呼吸平順度與水分補充。",
            possibleCauses = listOf(
                PossibleCause(
                    name = "急性上呼吸道感染 (普通感冒 / 病毒性感冒)",
                    likelihood = "高度常見",
                    explanation = "多種呼吸道病毒引起，通常表現為喉嚨痛、流鼻水、打噴嚏、輕中度咳嗽。",
                    keySigns = listOf("喉嚨乾痛", "鼻塞流涕", "輕度全身倦怠")
                ),
                PossibleCause(
                    name = "流行性感冒 (Influenza) / COVID-19",
                    likelihood = "常見可能",
                    explanation = "全身性症狀較顯著，常有突發高燒、全身肌肉酸痛、畏寒與極度疲倦。",
                    keySigns = listOf("突發高燒", "全身肌肉酸痛", "明顯倦怠")
                ),
                PossibleCause(
                    name = "急性支氣管炎 / 肺炎早期",
                    likelihood = "需持續監測",
                    explanation = "若咳嗽超過一週、有黃綠濃痰或伴隨胸骨後悶痛、發燒不退，需防下呼吸道侵犯。",
                    keySigns = listOf("黃綠色濃痰", "胸悶氣促", "持續高熱")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("耳鼻喉科 / 家醫科 / 胸腔內科", "一般門診診治或快篩抗病毒藥物評估", "常規門診"),
                DepartmentRecommendation("急診醫學科", "若出現血氧下降、喘鳴嚴重或唇色發紫時", "緊急")
            ),
            redFlagWarnings = listOf(
                "安靜呼吸時明顯費力、胸部凹陷或講話無法連貫成句",
                "指甲或嘴唇泛發青紫色（發紺，缺氧表徵）",
                "連續高燒大於 39°C 超過 72 小時不退",
                "咳出大量鮮血或呼吸衰竭"
            ),
            homeCareAdvice = listOf(
                "多喝溫開水，維持呼吸道黏膜濕潤以利痰液稀釋排出",
                "充足睡眠與休息，避免劇烈運動",
                "喉嚨痛可使用溫鹽水漱口或含喉糖舒緩局部刺激",
                "外出配戴口罩，保護家人並防範交叉感染"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }

    private fun analyzeDizziness(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "diz_q1",
                question = "頭暈的感覺比較像下列哪一種？",
                purpose = "鑑別旋轉性眩暈 (周邊前庭問題) 與非旋轉性頭昏/昏厥前兆",
                options = listOf("天旋地轉，閉眼感覺四周在轉動 (眩暈)", "頭重腳輕、浮浮的或走路像踩棉花", "坐著站起來瞬間眼前發黑、快昏倒", "伴隨耳鳴或耳朵悶塞感")
            ),
            FollowUpQuestion(
                id = "diz_q2",
                question = "是否有特定的姿勢變換會誘發眩暈？",
                purpose = "鑑別良性陣發性姿勢性眩暈 (耳石症 BPPV)",
                options = listOf("在床上翻身、抬頭或彎腰時突發幾十秒劇烈旋轉", "與姿勢無關，一直持續暈著", "走動時才明顯不穩", "無特定誘發姿勢")
            )
        )

        return AiAnalysisResult(
            triageLevel = TriageLevel.ROUTINE_CLINIC,
            summary = "您反映了頭暈/眩暈症狀。頭暈涉及內耳前庭平衡系統、血液循環與腦神經系統，需細分旋轉性或姿勢性以利精準鑑別。",
            possibleCauses = listOf(
                PossibleCause(
                    name = "良性陣發性姿勢性眩暈 (耳石脫落症)",
                    likelihood = "高度常見",
                    explanation = "內耳半規管耳石移位，通常在頭部姿勢大幅度改變（如翻身、抬頭）時誘發短暫劇烈旋轉感。",
                    keySigns = listOf("翻身抬頭引發", "旋轉眩暈數十秒", "常伴噁心")
                ),
                PossibleCause(
                    name = "前庭神經炎 / 內耳迷路炎",
                    likelihood = "中度可能",
                    explanation = "前庭神經受病毒感染發炎，表現為持續數天的強烈眩暈、步態不穩與噁心嘔吐。",
                    keySigns = listOf("持續性強烈眩暈", "眼球震顫", "劇烈噁心")
                ),
                PossibleCause(
                    name = "姿態性低血壓 / 血液循環暫時不足",
                    likelihood = "常見良性",
                    explanation = "從躺坐姿快速站起時，血液回流短暫不及造成腦部微缺血，眼前發黑頭輕。",
                    keySigns = listOf("起身瞬間眼前發黑", "稍坐片刻即緩解")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("耳鼻喉科 (眩暈專科)", "進行前庭平衡功能測試、聽力檢測與耳石復位術", "常規門診"),
                DepartmentRecommendation("神經內科", "排除後腦幹循環或中樞性眩暈", "門診")
            ),
            redFlagWarnings = listOf(
                "眩暈合併口齒不清、複視（看到雙影）、吞嚥困難或手腳癱軟",
                "突發性單側聽力喪失",
                "無法自行站立行走且劇烈嘔吐不止"
            ),
            homeCareAdvice = listOf(
                "眩暈發作時請立刻就地坐下或平躺，避免跌倒受傷",
                "改變姿勢（如早晨起床）時動作放慢，先在床緣靜坐 1 分鐘再站起",
                "避免突然劇烈轉頭或快速低頭俯身",
                "保持水分補給，減少高鹽食物及咖啡因攝取"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }

    private fun analyzeGeneral(input: InitialSymptomInput, text: String, isFinal: Boolean, round: Int): AiAnalysisResult {
        val isPainHigh = input.painLevel >= 8
        val level = if (isPainHigh) TriageLevel.PROMPT else TriageLevel.SELF_CARE

        val followUps = if (isFinal || round >= 3) emptyList() else listOf(
            FollowUpQuestion(
                id = "gen_q1",
                question = "這個不適症狀最早是從什麼時候開始的？是否有逐漸加重的趨勢？",
                purpose = "掌握疾病病程與進展速度",
                options = listOf("今天突然開始且越來越嚴重", "已經持續數天至一週左右", "斷斷續續超過兩週以上", "時好時壞，無明顯變化")
            ),
            FollowUpQuestion(
                id = "gen_q2",
                question = "除了上述描述外，您目前是否有以下全身性狀況？",
                purpose = "評估是否涉及全身感染、自律神經或代謝問題",
                options = listOf("體溫偏高或有發燒畏寒感", "食慾明顯變差、體重無故減輕", "睡眠品質嚴重受損或疲勞難消", "目前食慾與睡眠正常")
            )
        )

        return AiAnalysisResult(
            triageLevel = level,
            summary = "已分析您的不適症狀。目前初步判斷可先進行自我觀察或常規門診諮詢，並透過補充線索以協助更深入研判。",
            possibleCauses = listOf(
                PossibleCause(
                    name = "暫時性機能不適 / 疲勞積累",
                    likelihood = "可能原因",
                    explanation = "作息壓力、免疫波動或體力透支引發之短期生理反應。",
                    keySigns = listOf("睡眠後通常有緩解", "無特定器質性劇痛")
                ),
                PossibleCause(
                    name = "輕微病毒感染反應",
                    likelihood = "待觀察鑑別",
                    explanation = "常見病毒感染早期可能僅有全身性無力或局部模糊不適感。",
                    keySigns = listOf("全身輕微酸軟", "低溫或畏風")
                )
            ),
            followUpQuestions = followUps,
            recommendedDepartments = listOf(
                DepartmentRecommendation("家庭醫學科 / 一般內科", "適合進行第一線整體評估與必要之理學檢查", "常規門診")
            ),
            redFlagWarnings = listOf(
                "突發劇烈不可耐之疼痛或意識模糊",
                "呼吸窘迫、嘴唇發黑或抽搐",
                "持續性高燒（大於 38.5°C）超過 3 日不退"
            ),
            homeCareAdvice = listOf(
                "保證充足睡眠，避免熬夜與過度勞累",
                "適當補充溫開水與均衡營養",
                "若症狀在 48 小時內未見改善或出現惡化，請就近前往家醫科就診"
            ),
            isComprehensive = isFinal,
            inquiryRound = round
        )
    }
}
