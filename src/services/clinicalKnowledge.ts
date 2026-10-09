import { AiAnalysisResult, InitialSymptomInput, TriageLevel } from '../types';

export class ClinicalKnowledgeEngine {
  static analyze(
    input: InitialSymptomInput,
    history: Array<{ speaker: string; text: string }>,
    isFinal: boolean,
    round: number
  ): AiAnalysisResult {
    const fullText = (
      input.description +
      ' ' +
      input.duration +
      ' ' +
      input.chronicConditions +
      ' ' +
      history.map(h => h.text).join(' ')
    ).toLowerCase();

    const isChest = fullText.includes('胸') || fullText.includes('心悸') || fullText.includes('喘') || fullText.includes('心跳');
    const isAbdominal = fullText.includes('腹') || fullText.includes('胃') || fullText.includes('肚') || fullText.includes('盲腸') || fullText.includes('闌尾');
    const isHeadache = fullText.includes('頭痛') || fullText.includes('頭暈') || fullText.includes('偏頭痛') || fullText.includes('頭重');
    const isResp = fullText.includes('咳') || fullText.includes('喉嚨') || fullText.includes('發燒') || fullText.includes('感冒');
    const isDizzy = fullText.includes('暈') || fullText.includes('眩暈') || fullText.includes('站不穩');

    if (isChest) return this.analyzeChest(input, fullText, isFinal, round);
    if (isAbdominal) return this.analyzeAbdominal(input, fullText, isFinal, round);
    if (isHeadache) return this.analyzeHeadache(input, fullText, isFinal, round);
    if (isResp) return this.analyzeResp(input, fullText, isFinal, round);
    if (isDizzy) return this.analyzeDizzy(input, fullText, isFinal, round);

    return this.analyzeGeneral(input, fullText, isFinal, round);
  }

  private static analyzeChest(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const isSevere = text.includes('壓迫') || text.includes('石頭壓') || text.includes('放射') || text.includes('冷汗') || text.includes('左肩') || input.painLevel >= 8;
    const triageLevel: TriageLevel = isSevere ? 'EMERGENCY' : 'PROMPT';

    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'chest_q1',
        question: '胸部不適的痛感最接近下列哪一種描述？',
        purpose: '鑑別心肌缺血 vs 食道反流 vs 胸壁神經痛',
        options: ['重物壓迫或窒息感 (痛在骨頭後方)', '尖銳刺痛或隨深呼吸姿勢改變', '胸口如火燒般灼熱、泛酸水', '突發劇烈撕裂感']
      },
      {
        id: 'chest_q2',
        question: '痛感是否有擴散延伸至其他部位？',
        purpose: '評估急性冠心症放射痛路徑',
        options: ['延伸到下巴、頸部或左手臂', '延伸至後背肩胛骨間', '局限在胸骨單點無擴散', '不確定']
      }
    ];

    return {
      triageLevel,
      summary: '胸部不適涉及心血管與胸腔重大器官，不可掉以輕心。目前評估需優先鑑別冠狀動脈缺血、胃食道逆流或胸廓肌肉炎。',
      possibleCauses: [
        {
          name: '心絞痛 / 冠狀動脈缺血',
          likelihood: isSevere ? '高度懷疑需排除' : '需列入鑑別',
          explanation: '心肌需氧量增加而血液供應不足引起，典型為胸骨後悶痛壓迫，常因運動、緊張誘發，休息後緩解。',
          keySigns: ['壓榨悶痛', '冒冷汗', '放射至左臂/下巴']
        },
        {
          name: '胃食道逆流 (GERD)',
          likelihood: '常見可能',
          explanation: '胃酸反流刺激食道黏膜引起灼熱感，易發生於飯後或平臥時。',
          keySigns: ['火燒心', '喉頭異物感', '口泛酸水']
        },
        {
          name: '胸壁肌筋膜炎 / 肋軟骨炎',
          likelihood: '良性鑑別',
          explanation: '姿勢不良或外力拉扯引發之肌肉神經炎，深呼吸或局部按壓時疼痛加劇。',
          keySigns: ['按壓痛明顯', '活動時牽扯痛']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '心臟血管內科', reason: '安排心電圖、心肌酵素與心臟超音波檢查', urgency: isSevere ? '緊急' : '儘速' },
        { name: '急診醫學科', reason: '若持續胸悶超過15分鐘或冒冷汗，請立赴急診', urgency: '緊急' }
      ],
      redFlagWarnings: [
        '持續性胸骨後壓迫窒息感超過 15 分鐘未緩解',
        '胸痛同時伴隨大量冷汗、呼吸極度困難或昏厥',
        '突發劇烈撕裂性胸背痛'
      ],
      homeCareAdvice: [
        '立刻停止進行中的所有活動，採取舒適半坐臥姿放鬆休息',
        '解開領口及束身衣物，保持呼吸道通暢',
        '若醫師曾處方硝化甘油 (NTG) 可依醫囑含服；若未曾開立請勿擅服他人藥品',
        '若症狀持續或惡化，請立即撥打 119 送醫'
      ],
      disclaimer: '本 AI 評估為衛生教育與就醫分診參考，無法取代執業醫師專業診斷。突發心胸痛請立即就醫。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }

  private static analyzeAbdominal(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const isRightLower = text.includes('右下') || text.includes('盲腸') || text.includes('闌尾');
    const isUpper = text.includes('上腹') || text.includes('心窩') || text.includes('胃');
    const isSevere = text.includes('劇烈') || text.includes('反彈') || text.includes('冒冷汗') || input.painLevel >= 8;

    const triageLevel: TriageLevel = (isSevere || (isRightLower && text.includes('發燒')))
      ? 'EMERGENCY'
      : isRightLower
      ? 'PROMPT'
      : 'ROUTINE_CLINIC';

    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'abd_q1',
        question: '疼痛的確切主要位置在哪裡？是否有轉移？',
        purpose: '確認解剖部位對應器官（闌尾、膽囊、胃部、腸道）',
        options: ['最初在肚臍周圍，後來固定在右下腹', '肚臍上方心窩處', '右上腹（肋骨下方）', '全腹部瀰漫性疼痛']
      },
      {
        id: 'abd_q2',
        question: '輕壓肚子放開瞬間，或是跳躍/震動時，是否有劇烈彈痛感？',
        purpose: '檢查腹膜刺激徵候 (Rebound tenderness)',
        options: ['有明顯反彈震痛 (跳躍或手放開瞬間特別痛)', '只有按下去痛，放開不會更痛', '肚子摸起來很柔軟', '不確定']
      },
      {
        id: 'abd_q3',
        question: '是否有合併下列任何消化道或全身症狀？',
        purpose: '鑑別感染與出血發炎症狀',
        options: ['有微燒且完全沒有食慾', '嘔吐多次無法喝水', '腹瀉或解黑便', '無其他症狀']
      }
    ];

    return {
      triageLevel,
      summary: '您描述了腹部疼痛。腹部器官密集，初步鑑別包含急性闌尾炎、胃炎/十二指腸潰瘍與急性腸胃炎，需注意腹膜刺激徵候與發炎進展。',
      possibleCauses: [
        {
          name: isRightLower ? '急性闌尾炎 (盲腸炎)' : '急性腸胃炎',
          likelihood: isRightLower ? '高度需排查' : '常見可能',
          explanation: isRightLower
            ? '闌尾管腔阻塞繼發細菌發炎，典型症狀為肚臍轉移至右下腹痛、食慾減退與低度發熱。'
            : '病毒或細菌侵犯胃腸道黏膜，常有陣發性腹絞痛、腹瀉或反胃。',
          keySigns: ['右下腹固定壓痛', '跳躍震痛', '厭食反胃']
        },
        {
          name: '急性胃炎 / 消化性潰瘍',
          likelihood: isUpper ? '高度可能' : '待鑑別',
          explanation: '胃酸刺激或胃壁黏膜發炎，多位於上腹心窩，可表現為空腹痛或餐後隱痛。',
          keySigns: ['上腹灼痛悶脹', '反胃噯氣']
        },
        {
          name: '膽囊炎 / 膽管結石',
          likelihood: '鑑別考量',
          explanation: '常見於油膩飲食後發作，右上腹陣發性劇痛並可能放射至右肩。',
          keySigns: ['右上腹壓痛', '發燒畏寒']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '肝膽腸胃科 / 一般外科', reason: '進行腹部觸診、超音波與血液白血球發炎指數檢測', urgency: triageLevel === 'EMERGENCY' ? '急診' : '今日/次日' },
        { name: '急診醫學科', reason: '若劇烈反彈痛、板狀腹或高燒，請立即就醫', urgency: '緊急' }
      ],
      redFlagWarnings: [
        '腹部緊繃如木板（板狀腹）或輕放開即引發劇痛（反彈痛）',
        '高燒大於 38.5°C 並合併劇烈發抖',
        '吐出咖啡色胃液或排出柏油黑便 / 鮮血便',
        '腹痛劇烈至休克、冒冷汗站立不穩'
      ],
      homeCareAdvice: [
        '在醫師尚未確定診斷前，【切勿】擅自服用止痛藥（特別是消炎止痛藥 NSAIDs），以免遮蔽闌尾穿孔或腹膜炎之關鍵病情',
        '暫時禁食空腹或僅少量小口飲溫水，減輕腸胃負擔並預備後續可能的檢查',
        '可採雙腿屈膝側臥姿勢，放鬆腹壁肌肉拉扯',
        '持續量測體溫並觀察疼痛範圍是否有擴散跡象'
      ],
      disclaimer: '本 AI 評估為衛生教育與就醫分診參考，無法取代醫師臨床觸診與超音波診斷。若症狀劇烈請速就醫。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }

  private static analyzeHeadache(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const isThunderclap = text.includes('爆炸') || text.includes('雷擊') || text.includes('一生最痛');
    const isNeuro = text.includes('手腳無力') || text.includes('嘴歪') || text.includes('說話不清') || text.includes('視力模糊');

    const triageLevel: TriageLevel = (isThunderclap || isNeuro || input.painLevel >= 9)
      ? 'EMERGENCY'
      : (text.includes('劇烈') || text.includes('嘔吐') || input.painLevel >= 7)
      ? 'PROMPT'
      : 'ROUTINE_CLINIC';

    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'head_q1',
        question: '這個頭痛發作到達最痛的高峰耗時多長？',
        purpose: '排除蜘蛛膜下腔出血(SAH)之雷擊性頭痛',
        options: ['數秒內瞬間達到最劇烈爆炸痛 (雷擊性)', '數十分鐘到數小時漸進性加劇', '整天或數天持續鈍痛緊繃', '睡眠醒來時最痛']
      },
      {
        id: 'head_q2',
        question: '頭痛時是否有伴隨下列哪種典型感覺？',
        purpose: '鑑別偏頭痛、緊縮型頭痛或顱內壓增高',
        options: ['單側如脈搏般跳動搏動痛，怕光怕吵噁心', '雙側整顆頭如緊箍咒勒住的緊繃鈍痛', '後頸部極度僵硬無法低頭碰胸', '沒有特殊伴隨感覺']
      },
      {
        id: 'head_q3',
        question: '是否有單側手腳麻木、嘴角歪斜或口齒不清？',
        purpose: '中風與急性腦神經學缺損評估 (FAST原則)',
        options: ['完全正常，無肢體無力或言語異常', '稍有頭暈但手腳肌力正常', '有感覺單側肢體軟弱無力 (危險警訊)', '偶有眼睛疲勞']
      }
    ];

    return {
      triageLevel,
      summary: '您提出了頭痛困擾。多數頭痛屬良性原發性頭痛（如緊縮型頭痛、偏頭痛），但必須第一時間排除腦血管疾病或顱內急性病變。',
      possibleCauses: [
        {
          name: '偏頭痛 (Migraine)',
          likelihood: '高度常見',
          explanation: '神經血管敏感反應，典型為單側搏動性抽痛，常伴隨畏光、怕吵、噁心反胃，走動時易加重。',
          keySigns: ['脈搏跳動感', '畏光怕吵', '噁心嘔吐']
        },
        {
          name: '緊縮型頭痛 (Tension-type)',
          likelihood: '高度常見',
          explanation: '多與肩頸肌肉疲乏、生活壓力或睡眠不足相關，表現為頭部雙側帶狀緊箍鈍痛。',
          keySigns: ['緊箍咒緊繃感', '肩頸僵硬', '通常無畏光噁心']
        },
        {
          name: '急性顱內病變 / 腦血管異常',
          likelihood: (isThunderclap || isNeuro) ? '急需排除' : '低度警示',
          explanation: '突發雷擊劇痛或伴隨神經學缺損，需由腦神經專科進行腦部電腦斷層排查。',
          keySigns: ['突發爆痛', '肢體無力', '意識改變']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '神經內科 (腦神經科)', reason: '診斷原發性頭痛型態或安排腦部影像檢查', urgency: '專科門診' },
        { name: '急診醫學科', reason: '若雷擊頭痛、劇烈嘔吐或四肢無力請立刻送醫', urgency: '緊急' }
      ],
      redFlagWarnings: [
        '突發瞬間達到頂點之一生中最劇烈爆炸性頭痛 (雷擊性頭痛)',
        '伴隨單側手腳麻木、嘴角歪斜、言語不清或視力缺損',
        '伴隨高燒且頸部極度僵硬無法低頭',
        '50歲以上新發生且持續惡化之頭痛'
      ],
      homeCareAdvice: [
        '在安靜、遮光的陰涼房間內平躺閉目休息',
        '可使用冷敷袋冷敷額頭或頸部，緩解搏動性血管擴張充血',
        '攝取適量溫水，避免熬夜或長時間盯螢幕',
        '記錄發作時間、誘發因素（飲食、氣候、壓力）供門診醫師參考'
      ],
      disclaimer: '本 AI 評估為衛生教育與就醫分診參考，無法取代神經專科醫師之實質診斷。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }

  private static analyzeResp(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const isSevere = text.includes('呼吸困難') || text.includes('喘不過氣') || text.includes('嘴唇發紫');
    const triageLevel: TriageLevel = isSevere ? 'EMERGENCY' : (text.includes('高燒') || input.painLevel >= 7) ? 'PROMPT' : 'SELF_CARE';

    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'resp_q1',
        question: '目前量測體溫大約幾度？發燒狀況如何？',
        purpose: '評估急性感染與發炎嚴重程度',
        options: ['體溫大於 38.5°C 且畏寒發抖', '輕度低燒 (37.5 ~ 38.4°C)', '無發燒，僅有局部呼吸道症狀', '吃了退燒藥後又反覆燒起']
      },
      {
        id: 'resp_q2',
        question: '咳嗽有痰嗎？呼吸時是否有喘鳴聲音？',
        purpose: '鑑別上呼吸道感染 vs 支氣管炎 / 肺炎 / 氣喘',
        options: ['乾咳無痰，喉嚨乾燥發癢', '有大量黃綠色濃痰', '咳嗽劇烈且呼吸時胸口有哮喘音', '偶爾咳嗽，痰呈清白透明']
      }
    ];

    return {
      triageLevel,
      summary: '您提到了呼吸道相關症狀。大多數為病毒性急性上呼吸道感染（如普通感冒、流感、COVID-19），應注意呼吸平穩度與水分補給。',
      possibleCauses: [
        {
          name: '急性上呼吸道感染 (普通感冒)',
          likelihood: '高度常見',
          explanation: '多由鼻病毒、腺病毒引起，以鼻塞、流涕、喉嚨乾痛、輕度咳嗽為主。',
          keySigns: ['喉嚨痛', '打噴嚏流涕', '輕度倦怠']
        },
        {
          name: '流行性感冒 (Influenza) / COVID-19',
          likelihood: '常見可能',
          explanation: '全身性症狀明顯，常見突發高熱、全身關節肌肉酸痛與重度疲勞。',
          keySigns: ['突發高燒', '全身酸痛', '畏寒明顯']
        },
        {
          name: '急性支氣管炎 / 早期肺炎',
          likelihood: '需觀察排查',
          explanation: '若持續咳嗽超過一週伴濃痰或胸骨後悶痛，需注意下呼吸道感染。',
          keySigns: ['黃綠濃痰', '呼吸短促']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '家醫科 / 耳鼻喉科 / 胸腔內科', reason: '門診評估或快篩抗病毒藥物開立', urgency: '常規門診' },
        { name: '急診醫學科', reason: '若出現血氧低下、呼吸喘鳴或嘴唇發紫時', urgency: '緊急' }
      ],
      redFlagWarnings: [
        '安靜呼吸時明顯費力、胸部凹陷或喘到無法講完一句話',
        '嘴唇或指甲呈青紫色（發紺，血氧嚴重不足）',
        '連續高燒大於 39°C 超過 3 日不退',
        '咳出大量鮮血或劇烈胸痛'
      ],
      homeCareAdvice: [
        '多飲溫開水，維持呼吸道濕潤以助痰液排出',
        '充分睡眠與臥床休息，暫停劇烈運動',
        '咽喉痛可使用溫鹽水漱口或含喉糖舒緩',
        '外出戴好口罩，注意手部衛生避免傳播'
      ],
      disclaimer: '本 AI 評估為健康衛教參考，不具醫師診斷效力。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }

  private static analyzeDizzy(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'diz_q1',
        question: '頭暈的具體感覺比較接近下列哪一種？',
        purpose: '鑑別旋轉性眩暈 (前庭系統) vs 非旋轉性頭昏/暈厥前兆',
        options: ['天旋地轉，感覺周遭物體或自己在旋轉 (眩暈)', '頭重腳輕、浮浮的或走路像踩在棉花上', '坐著起身瞬間眼前發黑、快昏倒', '伴隨耳鳴或耳朵悶塞感']
      },
      {
        id: 'diz_q2',
        question: '在床上翻身、抬頭或彎腰時，是否會劇烈誘發？',
        purpose: '鑑別良性陣發性姿勢性眩暈 (耳石脫落症 BPPV)',
        options: ['翻身或抬頭瞬間引發劇烈天旋地轉數十秒', '與姿勢變換無關，一直持續暈', '走動時明顯不平衡', '無特定姿勢關聯']
      }
    ];

    return {
      triageLevel: 'ROUTINE_CLINIC',
      summary: '您提出了頭暈/眩暈困擾。頭暈涉及內耳前庭平衡、血液循環與腦神經系統，需細分旋轉性或姿勢性以利精準鑑別。',
      possibleCauses: [
        {
          name: '良性陣發性姿勢性眩暈 (耳石脫落症 BPPV)',
          likelihood: '高度常見',
          explanation: '內耳半規管耳石移位，特定頭部姿勢變動時引發短暫強烈旋轉眩暈。',
          keySigns: ['翻身抬頭引發', '旋轉眩暈數十秒', '伴隨噁心']
        },
        {
          name: '前庭神經炎 / 內耳迷路炎',
          likelihood: '中度可能',
          explanation: '病毒感染導致前庭神經發炎，可表現為持續數天的強烈眩暈與步態不穩。',
          keySigns: ['持續性眩暈', '噁心嘔吐']
        },
        {
          name: '姿態性低血壓',
          likelihood: '良性常見',
          explanation: '由躺臥急速站立時血壓短暫不及回升，引發暫時性腦缺血眼前發黑。',
          keySigns: ['起身瞬間發黑', '稍坐片刻即改善']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '耳鼻喉科 (眩暈專科)', reason: '進行前庭平衡功能測試、聽力檢查與耳石復位治療', urgency: '常規門診' },
        { name: '神經內科', reason: '排除後腦循環缺血或中樞性神經病灶', urgency: '門診' }
      ],
      redFlagWarnings: [
        '眩暈合併講話口齒不清、複視、吞嚥困難或手腳麻木癱軟',
        '突發單側聽力急遽喪失',
        '無法自行站立行走且劇烈噴射性嘔吐'
      ],
      homeCareAdvice: [
        '眩暈發作時請立刻就地坐下或平躺，避免跌倒受傷',
        '改變姿勢時動作放慢，晨起可先在床沿靜坐 1 分鐘再站起',
        '避免突然劇烈轉頭或劇烈低頭俯身',
        '維持足量水分攝取，減少高鹽食物與咖啡因'
      ],
      disclaimer: '本 AI 評估為健康分診參考，無法取代專科醫師面診。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }

  private static analyzeGeneral(input: InitialSymptomInput, text: string, isFinal: boolean, round: number): AiAnalysisResult {
    const isSevere = input.painLevel >= 8;
    const triageLevel: TriageLevel = isSevere ? 'PROMPT' : 'SELF_CARE';

    const followUps = (isFinal || round >= 3) ? [] : [
      {
        id: 'gen_q1',
        question: '這個不舒服的症狀最早是何時開始的？是否有逐漸加劇？',
        purpose: '掌握病情時序與進展速度',
        options: ['今天突發且越來越明顯', '持續數天至一週左右', '斷斷續續超過兩週以上', '時好時壞無特定趨勢']
      },
      {
        id: 'gen_q2',
        question: '除了此症狀外，目前是否有全身性不適？',
        purpose: '評估是否合併全身感染或自律神經波動',
        options: ['感覺微燒畏寒', '食慾顯著減退、體重異常下降', '睡眠障礙與極度疲勞', '目前食慾與睡眠正常']
      }
    ];

    return {
      triageLevel,
      summary: '已初步分析您的不適狀況。目前研判可先進行自我觀察或前往常規門診，並透過補充線索以協助更深入研判。',
      possibleCauses: [
        {
          name: '暫時性機能不適 / 疲勞積累',
          likelihood: '常見可能',
          explanation: '生活壓力、睡眠欠佳或體力透支引起之生理反應。',
          keySigns: ['休息後通常緩解', '無特定器質性劇痛']
        },
        {
          name: '輕微病毒感染反應',
          likelihood: '待觀察鑑別',
          explanation: '病毒感染初期可能僅有輕度全身疲軟或局部隱約不適。',
          keySigns: ['全身微酸', '容易疲勞']
        }
      ],
      followUpQuestions: followUps,
      recommendedDepartments: [
        { name: '家庭醫學科 / 一般內科', reason: '適合做第一線整體評估與必要的理學檢查', urgency: '常規門診' }
      ],
      redFlagWarnings: [
        '突發劇烈不可耐之疼痛或意識模糊混亂',
        '呼吸極度急促困難、嘴唇發黑發紺',
        '持續性高燒（大於 38.5°C）超過 3 日不退'
      ],
      homeCareAdvice: [
        '保證充分睡眠，避免熬夜與過度勞累',
        '多飲溫開水並攝取均衡清淡營養',
        '若症狀持續超過 48 小時未改善或惡化，請至家醫科門診檢查'
      ],
      disclaimer: '本 AI 評估僅供參考，若感不適持續或劇烈請儘速就醫。',
      isComprehensive: isFinal,
      inquiryRound: round
    };
  }
}
