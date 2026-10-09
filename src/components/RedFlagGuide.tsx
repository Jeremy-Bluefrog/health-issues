import React from 'react';
import { PhoneCall, AlertTriangle, HeartPulse, Brain, Flame, Wind, ArrowRight } from 'lucide-react';

export const RedFlagGuide: React.FC = () => {
  return (
    <div className="space-y-5 max-w-3xl mx-auto p-4 sm:p-6 animate-fade-in">
      {/* M3 Emergency Callout Banner */}
      <div className="p-5 sm:p-6 rounded-[28px] bg-[#ba1a1a] text-white shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center text-xs font-bold uppercase tracking-wider text-[#ffdad6] mb-1.5">
            <AlertTriangle className="w-4 h-4 mr-1 text-white" />
            生命緊急危難請即刻通報
          </div>
          <div className="text-2xl sm:text-3xl font-extrabold flex items-center">
            <PhoneCall className="w-7 h-7 mr-2.5" />
            119 緊急救護專線
          </div>
          <p className="text-xs sm:text-sm text-[#ffdad6] mt-1.5 leading-relaxed">
            若突發意識模糊、休克大出血、劇烈胸痛窒息感或嚴重呼吸衰竭，不可等待，請立即撥打 119！
          </p>
        </div>
      </div>

      <div className="pt-2">
        <h2 className="text-lg font-bold text-[#191c1d]">
          常見不可輕忽之急症「紅旗危象」
        </h2>
        <p className="text-xs text-[#3f484a] mt-0.5">
          若出現以下徵候，建議立刻前往最近急診醫學科就醫評估
        </p>
      </div>

      {/* M3 Tonal Category Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        {/* Cardiovascular */}
        <div className="p-5 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-2xs space-y-2.5 hover:shadow-sm transition-all">
          <div className="flex items-center text-[#ba1a1a] font-bold text-sm">
            <HeartPulse className="w-5 h-5 mr-2 text-[#ba1a1a]" />
            心血管危急 (心肌梗塞)
          </div>
          <ul className="text-xs text-[#3f484a] space-y-1.5 leading-relaxed">
            <li>• 胸骨後石頭壓迫或窒息感超過 15 分鐘</li>
            <li>• 痛感放射至下巴、頸部或左手臂內側</li>
            <li>• 伴隨大量冒冷汗、臉色死白蒼白、瀕死感</li>
          </ul>
        </div>

        {/* Brain FAST */}
        <div className="p-5 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-2xs space-y-2.5 hover:shadow-sm transition-all">
          <div className="flex items-center text-[#525e7d] font-bold text-sm">
            <Brain className="w-5 h-5 mr-2 text-[#525e7d]" />
            急性腦中風 (FAST 辨識)
          </div>
          <ul className="text-xs text-[#3f484a] space-y-1.5 leading-relaxed">
            <li>• <b>Face</b> 請患者微笑，嘴角單側下垂不對稱</li>
            <li>• <b>Arm</b> 雙手向前平舉，單側手臂無力垂落</li>
            <li>• <b>Speech</b> 說話口齒不清、大舌頭或無法理解言語</li>
            <li>• <b>Time</b> 記下時間，搶爭黃金 3~4.5 小時溶栓</li>
          </ul>
        </div>

        {/* Acute Abdomen */}
        <div className="p-5 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-2xs space-y-2.5 hover:shadow-sm transition-all">
          <div className="flex items-center text-[#9c4300] font-bold text-sm">
            <Flame className="w-5 h-5 mr-2 text-[#9c4300]" />
            急腹症 (闌尾穿孔/腹膜炎)
          </div>
          <ul className="text-xs text-[#3f484a] space-y-1.5 leading-relaxed">
            <li>• 肚子摸起來堅硬如木板 (板狀腹)</li>
            <li>• 輕壓腹部放開瞬間引發劇烈彈痛 (反彈痛)</li>
            <li>• 吐出深咖啡色胃液或排出瀝青柏油黑便</li>
          </ul>
        </div>

        {/* Respiratory */}
        <div className="p-5 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-2xs space-y-2.5 hover:shadow-sm transition-all">
          <div className="flex items-center text-[#006874] font-bold text-sm">
            <Wind className="w-5 h-5 mr-2 text-[#006874]" />
            呼吸危象與嚴重過敏
          </div>
          <ul className="text-xs text-[#3f484a] space-y-1.5 leading-relaxed">
            <li>• 說話無法完整成句，呼吸肋間明顯凹陷</li>
            <li>• 嘴唇或指甲床呈現青紫色 (發紺缺氧)</li>
            <li>• 全身大面積尋常紅疹伴隨喉頭水腫呼吸困難</li>
          </ul>
        </div>
      </div>

      {/* Doctor Communication Tips */}
      <div className="p-5 rounded-[24px] bg-[#f2f5f6] border border-[#bfc8ca]/60 text-xs sm:text-sm text-[#191c1d] space-y-3">
        <div className="font-bold text-[#191c1d] flex items-center text-sm">
          向醫師精準陳述病況的 4 大重點：
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-[#3f484a]">
          <div className="p-2.5 rounded-[12px] bg-white border border-[#bfc8ca]/40">
            <span className="font-bold text-[#006874]">1. 時間軸：</span>何時開始？是瞬間爆痛還是漸進加重？持續多久？
          </div>
          <div className="p-2.5 rounded-[12px] bg-white border border-[#bfc8ca]/40">
            <span className="font-bold text-[#006874]">2. 部位與性質：</span>悶痛、絞痛、刺痛還是灼熱？能否用一指點出最痛處？
          </div>
          <div className="p-2.5 rounded-[12px] bg-white border border-[#bfc8ca]/40">
            <span className="font-bold text-[#006874]">3. 誘發與緩解：</span>飯後、空腹、走路跳躍、深呼吸時是否會加劇？
          </div>
          <div className="p-2.5 rounded-[12px] bg-white border border-[#bfc8ca]/40">
            <span className="font-bold text-[#006874]">4. 過去病史：</span>有無高血壓/糖尿病、固定用藥或特殊藥物過敏史。
          </div>
        </div>
      </div>
    </div>
  );
};
