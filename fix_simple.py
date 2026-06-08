with open("D:\\vueprojects\\RX\\ui\\src\\layout\\index.vue", "r", encoding="utf-8") as f:
    c = f.read()

old = """      userStore.logout()
      tagsStore.removeAllViews()
      // 下一事件循环执行导航，确保所有状态已清空
      setTimeout(() => {
        router.push('/login').catch(() => {
          window.location.href = '/login'
        })
      }, 50)"""

new = """      userStore.logout()
      tagsStore.removeAllViews()
      router.push('/login')"""

# Check if the old string exists
if old in c:
    print("Found and replacing")
    c = c.replace(old, new)
else:
    print("String not found, checking what's there")
    import re
    match = re.search(r'userStore\.logout\(\)(.+?)router\.push', c, re.DOTALL)
    if match:
        print(repr(match.group(0)[:200]))
    else:
        # Search for tagsStore
        idx = c.find("tagsStore.removeAllViews()")
        if idx > 0:
            print("Found tagsStore at", idx)
            print(repr(c[idx:idx+200]))

with open("D:\\vueprojects\\RX\\ui\\src\\layout\\index.vue", "w", encoding="utf-8") as f:
    f.write(c)
print("Done")
