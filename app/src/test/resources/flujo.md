# FLUJO

| Año | Pregunta / Opción / Cuadro | Condición | Acción | Destino | Instruccion |
| --- | --- | --- | --- | --- | --- |
| 2026 | P300-301-N | código = 1 || 2 || 3 ||12 | Pase a | P300-302 | P300-302 se responde si la persona tiene más de 15 años |
|  | P300-301-N | código = 4 || 5 | Pase a | P300-303 | — |
|  | P300-301-N | código = 6 || 7 || 8 || 9 || 10|| 13|| 14 | Continúa con | P300-301A | — |
| 2026 | P300-301A-3 | código = 1 | Pase a | P300-303 | — |
|  | P300-301A-3 | código = 2 | Continúa con | P300-301B | — |
|  | P300-303 | código = 1 | Continúa con | P300-304 | — |
|  | P300-303 | código = 2 | Pase a | P300-306 | — |
| 2026 | P300-306 | código = 1 | Continúa con | P300-308 | — |
| 2026 | P300-306 | código = 2 | Pase a | P300-307D | P300-307D se responde solo si P300-301-N=3||4||5||6   o P300-304-N=2||3  o P300-308-N=2||3 |
| 2026 | P300-310B1 | código = 1 | Continúa con | P300-310C1 | — |
| 2026 | P300-310B1 | código = 2 | Pase a | A | El cuadro A se ubica despues de P300-310E |
| 2026 | A | P300-303 = 2 || P300-306 = 2 || P300-310A = 2 | Continúa con | B | El cuadro B se ubica despues de P300-312 |
| 2026 | A | P300-303 <> 2 &&  P300-306 <> 2 && P300-310A <> 3 | Pase a | P300-311 | — |
| 2026 | B | P300-306 = 2 || P300-307 = 2  | Continúa con | P300-313A | P300-313A se responde si la persona es menor de 25 años |
| 2026 | B | P300-306 = 1 <> P300-307 = 1 | Pase a | P300-314A | — |
| 2026 | P300-314A | código = 1 | Continúa con | P300-314B | — |
| 2026 | P300-314A | código = 2 | Pase a | P300-316A | — |
| 2026 | P300-314B | código = 4 | Pase a | P300-315 | Si alguna alternativa corresponde a código 4 "Cabina Pública" |
| 2026 | P300-314B | código <> 4 | Pase a | P300-316 | — |
| 2026 | P300-315 | código = 1 | Continúa con | P300-315A | — |
| 2026 | P300-315 | código <> 1 | Pase a | P300-315B | — |
| 2026 | P300-316B | código = 1 | Continúa con | P300-316C | — |
| 2026 | P300-316B | código = 2 | Pase a | CAPÍTULO 400 | — |