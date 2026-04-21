package com.library.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 图书封面生成控制器
 * 动态生成SVG格式的图书封面占位图，无需依赖外部图片服务
 */
@RestController
@RequestMapping("/api/covers")
public class CoverController {

    // 预定义的渐变色方案
    private static final String[][] COLOR_SCHEMES = {
            {"#DC2626", "#991B1B"},  // 红
            {"#EA580C", "#9A3412"},  // 橙
            {"#D97706", "#92400E"},  // 琥珀
            {"#16A34A", "#166534"},  // 绿
            {"#2563EB", "#1E3A8A"},  // 蓝
            {"#7C3AED", "#4C1D95"},  // 紫
            {"#DB2777", "#9D174D"},  // 粉
            {"#0891B2", "#155E75"},  // 青
            {"#4F46E5", "#312E81"},  // 靛蓝
            {"#059669", "#064E3B"},  // 翡翠
    };

    @GetMapping(value = "/generate", produces = "image/svg+xml")
    public ResponseEntity<String> generateCover(
            @RequestParam(defaultValue = "图书") String title,
            @RequestParam(defaultValue = "") String author,
            @RequestParam(required = false) Integer colorIndex) {

        // 根据书名hash选择配色
        int idx = colorIndex != null ? colorIndex % COLOR_SCHEMES.length
                : Math.abs(title.hashCode()) % COLOR_SCHEMES.length;
        String[] colors = COLOR_SCHEMES[idx];

        // 处理标题：如果太长则截断并换行
        String titleLine1, titleLine2 = "";
        if (title.length() > 6) {
            titleLine1 = title.substring(0, Math.min(6, title.length()));
            titleLine2 = title.substring(6, Math.min(12, title.length()));
            if (title.length() > 12) titleLine2 += "…";
        } else {
            titleLine1 = title;
        }

        String svg = String.format("""
                <svg xmlns="http://www.w3.org/2000/svg" width="400" height="600" viewBox="0 0 400 600">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect width="400" height="600" fill="url(#bg)" rx="8"/>
                  <rect x="30" y="30" width="340" height="540" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="2" rx="4"/>
                  <text x="200" y="%d" text-anchor="middle" fill="white" font-family="SimHei,Microsoft YaHei,sans-serif" font-size="48" font-weight="bold">%s</text>
                  %s
                  <text x="200" y="%d" text-anchor="middle" fill="rgba(255,255,255,0.7)" font-family="SimHei,Microsoft YaHei,sans-serif" font-size="24">%s</text>
                  <line x1="150" y1="%d" x2="250" y2="%d" stroke="rgba(255,255,255,0.3)" stroke-width="2"/>
                  <text x="200" y="540" text-anchor="middle" fill="rgba(255,255,255,0.4)" font-family="sans-serif" font-size="14">智慧图书馆</text>
                </svg>
                """,
                colors[0], colors[1],
                titleLine2.isEmpty() ? 280 : 260, escapeXml(titleLine1),
                titleLine2.isEmpty() ? "" :
                        String.format("<text x=\"200\" y=\"320\" text-anchor=\"middle\" fill=\"white\" font-family=\"SimHei,Microsoft YaHei,sans-serif\" font-size=\"48\" font-weight=\"bold\">%s</text>", escapeXml(titleLine2)),
                titleLine2.isEmpty() ? 340 : 380, escapeXml(author),
                titleLine2.isEmpty() ? 310 : 350, titleLine2.isEmpty() ? 310 : 350
        );

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .header("Cache-Control", "public, max-age=86400")
                .body(svg);
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
