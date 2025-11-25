import json
import random
from datetime import datetime, timedelta

users = ["player" + str(i) for i in range(1, 16)]  # 15 jugadores
data = []

for user in users:
    edad = random.randint(18, 30)
    # Cada jugador tiene entre 1 y 5 sesiones
    num_sessions = random.randint(1, 5)
    for s in range(num_sessions):
        rondas = random.choice([5, 10, 15])
        dificultad = random.choice([1, 2, 3])
        aciertos = random.randint(0, rondas)
        errores = rondas - aciertos
        fechaInicio = datetime(2025, 11, 20) + timedelta(days=random.randint(0, 5), hours=random.randint(6, 18))
        game_time = rondas * random.randint(50, 100)  # tiempo total aproximado
        fechaFin = fechaInicio + timedelta(seconds=game_time)

        session = {
            "username": user,
            "edad": edad,
            "rondas": rondas,
            "dificultad": dificultad,
            "aciertos": aciertos,
            "errores": errores,
            "fechaHoraInicio": fechaInicio.isoformat(),
            "fechaHoraFin": fechaFin.isoformat(),
            "gameTime": game_time
        }
        data.append(session)

# Limitar a unos 50 registros
data = data[:50]

# Guardar JSON
with open("game_data.json", "w") as f:
    json.dump(data, f, indent=2)

print("JSON de prueba generado con", len(data), "registros.")