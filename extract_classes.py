import re

file_path = r'd:\OneDrive-ntdxl\prj\cbgp\cbgp\src\simple\Main.java'
try:
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    with open('output.txt', 'w', encoding='utf-8') as out:
        for i, line in enumerate(lines):
            sline = line.strip()
            if re.match(r'^(public\s+|abstract\s+|final\s+)*(class|interface)\s+\w+', sline):
                out.write(f"Line {i+1}: CLASS/INTERFACE {sline}\n")
            elif re.match(r'^(public\s+|protected\s+|private\s+)?(static\s+)?[\w<>,\[\]\s]+\s+\w+\s*\(.*?\)\s*\{?', sline):
                if 'if ' not in sline and 'for ' not in sline and 'while ' not in sline and 'catch ' not in sline and 'switch ' not in sline:
                    if sline.endswith('{') or (i+1 < len(lines) and lines[i+1].strip().startswith('{')):
                        out.write(f"  Line {i+1}: METHOD {sline}\n")

except Exception as e:
    print(e)
