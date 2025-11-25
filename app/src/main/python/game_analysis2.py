import json
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, confusion_matrix

# ===============================================================
# 1️⃣ CARGA Y LIMPIEZA DESDE JSON
# ===============================================================
def load_and_clean_from_json(json_path="game_data.json"):
    """
    Carga datos directamente desde game_data.json y devuelve un DataFrame limpio.
    """
    with open(json_path, "r") as f:
        data = json.load(f)

    if not isinstance(data, list):
        raise ValueError("El JSON debe contener una lista de sesiones.")

    df = pd.DataFrame(data)

    # Convertir fechas
    df["fechaHoraInicio"] = pd.to_datetime(df["fechaHoraInicio"])
    df["fechaHoraFin"] = pd.to_datetime(df["fechaHoraFin"])

    # Calcular gameTime si no existe
    df["gameTime"] = df["gameTime"].fillna(
        (df["fechaHoraFin"] - df["fechaHoraInicio"]).dt.total_seconds()
    )

    # Filtrar sesiones inválidas
    df = df[df["gameTime"] > 0]
    df.drop_duplicates(inplace=True)

    return df

# ===============================================================
# 2️⃣ VISUALIZACIONES
# ===============================================================
def create_plots(df):
    # Histograma duración de sesión
    plt.figure()
    plt.hist(df["gameTime"], bins=20)
    plt.title("Distribución del tiempo de sesión")
    plt.xlabel("Tiempo (s)")
    plt.ylabel("Frecuencia")
    plt.savefig("hist_session_length.png")

    # Scatter errores vs aciertos
    plt.figure()
    plt.scatter(df["errores"], df["aciertos"])
    plt.title("Errores vs Aciertos")
    plt.xlabel("Errores")
    plt.ylabel("Aciertos")
    plt.savefig("scatter_errors_vs_aciertos.png")

    # Puntuación media por dificultad
    df["puntuacion"] = df["aciertos"] - df["errores"]
    plt.figure()
    df.groupby("dificultad")["puntuacion"].mean().plot(kind="bar")
    plt.title("Puntuación media por dificultad")
    plt.xlabel("Dificultad")
    plt.ylabel("Puntuación")
    plt.savefig("bar_puntuacion_dificultad.png")

# ===============================================================
# 3️⃣ MODELO DE PREDICCIÓN: ¿VOLVERÁ A JUGAR?
# ===============================================================
def train_model(df):
    # Marcar jugadores recurrentes
    session_counts = df.groupby("username")["rondas"].count()
    df["returning_player"] = df["username"].map(lambda u: 1 if session_counts[u] > 1 else 0)

    # Variables predictoras
    features = ["rondas", "dificultad", "aciertos", "errores", "gameTime"]
    X = df[features]
    y = df["returning_player"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    model = LogisticRegression(max_iter=500)
    model.fit(X_train, y_train)
    preds = model.predict(X_test)

    print("Precisión:", accuracy_score(y_test, preds))
    print("Matriz de confusión:\n", confusion_matrix(y_test, preds))

    return model

# ===============================================================
# 4️⃣ FUNCIÓN PRINCIPAL PARA ANDROID / CHAQUOPY
# ===============================================================
def run(json_path="game_data.json"):
    df = load_and_clean_from_json(json_path)
    create_plots(df)
    train_model(df)
    return "OK"

# ===============================================================
# 5️⃣ EJECUCIÓN LOCAL
# ===============================================================
if __name__ == "__main__":
    run()
