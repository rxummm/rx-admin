"""
RX Admin 菜单自动扫描脚本
需求：21.4 文档维护自动化 - 扫描 sys_menu 表生成模块清单

用法:
  python scan_menus.py [--db-url=jdbc:mysql://...] [--username=root] [--password=...]

默认连接参数来自环境变量或内置默认值。
输出：Markdown 格式的模块清单表格。
"""

import sys
import os
import argparse

# ========== 配置（优先使用环境变量） ==========
DB_HOST = os.environ.get("RX_DB_HOST", "localhost")
DB_PORT = os.environ.get("RX_DB_PORT", "3306")
DB_NAME = os.environ.get("RX_DB_NAME", "rx_admin")
DB_USER = os.environ.get("RX_DB_USER", "root")
DB_PASS = os.environ.get("RX_DB_PASS", "123456")

SQL_MENU = """
SELECT
    id,
    parent_id,
    menu_name AS name,
    path,
    component,
    menu_type,
    perms,
    icon,
    sort_order,
    visible,
    status,
    remark,
    CASE menu_type
        WHEN 1 THEN '目录'
        WHEN 2 THEN '菜单'
        WHEN 3 THEN '按钮'
        ELSE '未知'
    END AS type_name,
    CASE status
        WHEN 1 THEN '启用'
        WHEN 0 THEN '禁用'
    END AS status_name
FROM sys_menu
WHERE deleted = 0
ORDER BY sort_order ASC, id ASC
"""

SQL_COUNT_MENU = "SELECT COUNT(*) AS total FROM sys_menu WHERE deleted = 0"
SQL_COUNT_BY_TYPE = """
SELECT menu_type, COUNT(*) AS cnt
FROM sys_menu WHERE deleted = 0
GROUP BY menu_type ORDER BY menu_type
"""


def try_connect():
    """尝试导入并使用多种数据库驱动连接。"""
    urls_to_try = [
        f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}",
        f"mysql+mysqlconnector://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}",
    ]

    # 优先尝试 pymysql
    try:
        import pymysql
        return "pymysql", f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"
    except ImportError:
        pass

    try:
        import mysql.connector
        return "mysql", f"mysql+mysqlconnector://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"
    except ImportError:
        pass

    # fallback: JDBC 格式供手动连接
    return None, f"jdbc:mysql://{DB_HOST}:{DB_PORT}/{DB_NAME}"


def print_help_and_exit():
    print("""
============================================================
  RX Admin 菜单扫描工具 — 数据库直连模式
============================================================

本脚本通过 PyMySQL / mysql-connector-python 直连数据库，
扫描 sys_menu 表并生成 Markdown 模块清单。

需先安装依赖：
  pip install pymysql sqlalchemy      # 推荐方式
  pip install mysql-connector-python  # 备选方式

环境变量设置：
  set RX_DB_HOST=localhost       (默认: localhost)
  set RX_DB_PASS=your_password   (默认: 123456)

用法：
  python scan_menus.py

不依赖数据库的离线模式（需提供 SQL 导出文件）：
  python scan_menus.py --offline --sql-file=dump.sql
============================================================
""")
    sys.exit(0)


def scan_from_db():
    """从数据库扫描菜单表"""
    try:
        from sqlalchemy import create_engine, text
    except ImportError:
        print("[错误] 请先安装 sqlalchemy: pip install sqlalchemy pymysql")
        sys.exit(1)

    driver, url = try_connect()
    if not url.startswith("mysql+"):
        print(f"[错误] 无法导入 MySQL 驱动。请安装: pip install pymysql sqlalchemy")
        print(f"       或手动连接: {url}")
        sys.exit(1)

    print(f"[信息] 连接数据库: {DB_USER}@{DB_HOST}:{DB_PORT}/{DB_NAME}")
    try:
        engine = create_engine(url, echo=False, pool_pre_ping=True)
        with engine.connect() as conn:
            # 统计信息
            total = conn.execute(text(SQL_COUNT_MENU)).scalar()
            by_type = conn.execute(text(SQL_COUNT_BY_TYPE)).fetchall()
            print(f"\n## 菜单统计")
            print(f"- 总菜单数: {total}")
            type_names = {"1": "目录", "2": "菜单", "3": "按钮"}
            for mt, cnt in by_type:
                print(f"- {type_names.get(str(mt), str(mt))}: {cnt}")

            # 菜单树
            menus = conn.execute(text(SQL_MENU)).fetchall()
            print(f"\n## 菜单清单 ({total} 条)\n")
            print("| ID | 父ID | 名称 | 路径 | 组件 | 类型 | 状态 | 权限标识 | 备注 |")
            print("|----|------|------|------|------|------|------|----------|------|")
            for m in menus:
                mid = m.id or ""
                pid = m.parent_id or ""
                name = (m.name or "").replace("|", "\\|")
                path = (m.path or "")
                comp = (m.component or "")
                tname = m.type_name or ""
                sname = m.status_name or ""
                perms = (m.perms or "")
                remark = (m.remark or "")
                print(f"| {mid} | {pid} | {name} | {path} | {comp} | {tname} | {sname} | {perms} | {remark} |")

            # 模块分组汇总
            print(f"\n## 模块分组汇总（按一级目录）\n")
            root_menus = [m for m in menus if m.parent_id == 0]
            print("| 目录 | 旗下菜单数 | 旗下按钮数 | 路由路径 |")
            print("|------|-----------|-----------|---------|")
            for rm in root_menus:
                children = [m for m in menus if m.parent_id and m.parent_id == rm.id]
                menu_count = sum(1 for c in children if c.menu_type == 2)
                btn_count = sum(1 for c in children if c.menu_type == 3)
                print(f"| {rm.name} | {menu_count} | {btn_count} | {rm.path} |")

            print(f"\n---\n> 自动生成于: {__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
            print(f"> 数据源: {DB_HOST}:{DB_PORT}/{DB_NAME}")

    except Exception as e:
        print(f"[错误] 数据库连接失败: {e}")
        print(f"       连接 URL: {url}")
        sys.exit(1)


def scan_from_file(sql_file):
    """从 SQL 文件解析菜单（离线模式）"""
    import re
    with open(sql_file, "r", encoding="utf-8") as f:
        content = f.read()

    # 匹配 INSERT INTO `sys_menu` ... VALUES (...)
    pattern = re.compile(
        r"INSERT\s+INTO\s+`?sys_menu`?\s*\(.*?\)\s*VALUES\s*(\(.*?\))\s*;",
        re.IGNORECASE | re.DOTALL,
    )
    matches = pattern.findall(content)
    if not matches:
        print("[信息] 未找到 INSERT INTO 语句，尝试匹配单行 VALUES...")
        # 备选：匹配每条 VALUES
        pattern2 = re.compile(r"VALUES\s*\(\s*(.+?)\s*\)\s*[,;]", re.IGNORECASE)
        matches = pattern2.findall(content)

    if not matches:
        print("[错误] 无法从 SQL 文件中解析菜单数据")
        sys.exit(1)

    print(f"## 菜单清单（离线模式，共 {len(matches)} 条）\n")
    print("| 名称 | 路径 | 类型 | 权限标识 |")
    print("|------|------|------|----------|")
    for i, vals in enumerate(matches):
        fields = [f.strip().strip("'").strip('"') for f in vals.split(",")]
        # 典型字段顺序: id, parent_id, menu_name, path, component, menu_type, perms, ...
        if len(fields) >= 7:
            name = fields[2] if len(fields) > 2 else ""
            path = fields[3] if len(fields) > 3 else ""
            mtype = fields[5] if len(fields) > 5 else ""
            perms = fields[6] if len(fields) > 6 else ""
            type_map = {"1": "目录", "2": "菜单", "3": "按钮"}
            print(f"| {name} | {path} | {type_map.get(mtype, mtype)} | {perms} |")
        else:
            print(f"| (第{i+1}条解析失败) | | | |")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="RX Admin 菜单扫描工具")
    parser.add_argument("--db-url", help="JDBC 数据库 URL")
    parser.add_argument("--username", help="数据库用户名")
    parser.add_argument("--password", help="数据库密码")
    parser.add_argument("--offline", action="store_true", help="离线模式（从SQL文件解析）")
    parser.add_argument("--sql-file", help="SQL导出文件路径")
    args = parser.parse_args()

    if args.db_url:
        # 简单解析 JDBC URL
        import re
        m = re.match(r"jdbc:mysql://([^:/]+):(\d+)/(.+)", args.db_url)
        if m:
            DB_HOST, DB_PORT, DB_NAME = m.groups()
    if args.username:
        globals()["DB_USER"] = args.username
    if args.password:
        globals()["DB_PASS"] = args.password

    if args.offline:
        scan_from_file(args.sql_file or "src/main/resources/db/init.sql")
    else:
        try:
            scan_from_db()
        except SystemExit:
            raise
        except Exception:
            print_help_and_exit()
