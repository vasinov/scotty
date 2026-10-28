"""Check the built site's local links, assets and fragment identifiers."""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path("docs/.vitepress/dist").resolve()
BASE = "/scotty/"


class Page(HTMLParser):
    def __init__(self, path):
        super().__init__()
        self.links = []
        self.ids = set()
        self.feed(path.read_text())

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if "id" in attrs:
            self.ids.add(attrs["id"])
        key = {"a": "href", "img": "src", "script": "src", "link": "href"}.get(tag)
        if key and attrs.get(key):
            self.links.append(attrs[key])


pages = {p: Page(p) for p in ROOT.rglob("*.html")}
if not pages:
    raise SystemExit("No built HTML found; run the VitePress build first.")
errors = []
for path, page in pages.items():
    for link in page.links:
        url = urlsplit(link)
        if url.scheme or url.netloc:
            continue
        target_path = unquote(url.path)
        if target_path.startswith("/"):
            if not target_path.startswith(BASE):
                errors.append(f"{path.relative_to(ROOT)}: link escapes {BASE}: {link}")
                continue
            target = ROOT / target_path[len(BASE):]
        elif target_path:
            target = path.parent / target_path
        else:
            target = path
        if target.is_dir():
            target = target / "index.html"
        target = target.resolve()
        if not target.is_relative_to(ROOT) or not target.is_file():
            errors.append(f"{path.relative_to(ROOT)}: missing target: {link}")
        elif url.fragment and target in pages and unquote(url.fragment) not in pages[target].ids:
            errors.append(f"{path.relative_to(ROOT)}: missing anchor: {link}")
if errors:
    raise SystemExit("\n".join(errors))
print(f"Checked links, assets and anchors in {len(pages)} HTML pages.")
