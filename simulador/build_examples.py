"""Reconstrói os exemplos da interface após editar os arquivos Java."""
import json
from pathlib import Path
root = Path(__file__).resolve().parent
examples = {"Autônomo · encoder + IMU": (root/"examples/AutonomoDemo.java").read_text(encoding="utf-8"),
            "TeleOp · gamepad / teclado": (root/"examples/TeleOpDemo.java").read_text(encoding="utf-8")}
(root/"examples.json").write_text(json.dumps(examples,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
