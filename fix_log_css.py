import re

with open("D:/vueprojects/RX/ui/src/views/monitor/log/index.vue", "r", encoding="utf-8") as f:
    content = f.read()

# Replace the CSS section to fix layout issues
old_css_start = ".vtable-container { border: 1px solid var(--border-light); border-radius: 4px; margin-bottom: 12px; }"
new_css = """.vtable-container { border: 1px solid var(--border-light); border-radius: 4px; margin-bottom: 12px; width: 100%; overflow-x: auto; }
.vtable-header { display: flex; align-items: center; background: var(--el-fill-color-light); font-weight: 600; font-size: 13px; color: var(--text-regular); border-bottom: 1px solid var(--border-light); cursor: pointer; min-width: 100%; }
.vscroller-wrapper { position: relative; min-height: 200px; width: 100%; }
.vscroller { height: 500px; width: 100%; }
.sort-arrow { font-size: 10px; margin-left: 2px; }
.vrow { display: flex; align-items: center; font-size: 13px; border-bottom: 1px solid var(--border-lighter); width: 100%; }
.vrow.even { background: var(--el-fill-color-lighter); }
.vrow:hover { background: var(--bg-hover); }
.vc { padding: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; box-sizing: border-box; }
.vc-chk { width: 45px; text-align: center; flex-shrink: 0; }
.vc-id { width: 70px; flex-shrink: 0; }
.vc-user { width: 120px; flex-shrink: 0; }
.vc-mod { width: 120px; flex-shrink: 0; }
.vc-act { width: 100px; text-align: center; flex-shrink: 0; }
.vc-mth { flex: 1 1 auto; min-width: 150px; max-width: 400px; }
.vc-ip { width: 140px; flex-shrink: 0; }
.vc-sts { width: 80px; text-align: center; flex-shrink: 0; }
.vc-cst { width: 100px; text-align: right; padding-right: 12px; flex-shrink: 0; }
.vc-tim { width: 170px; flex-shrink: 0; }
.vc-op { width: 140px; text-align: center; flex-shrink: 0; }"""

content = content.replace(
    re.escape(old_css_start) + r".*?" + re.escape("</style>"),
    new_css + "\n" + "</style>",
    flags=re.DOTALL
)

with open("D:/vueprojects/RX/ui/src/views/monitor/log/index.vue", "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
