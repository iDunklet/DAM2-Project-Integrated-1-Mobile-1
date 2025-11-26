import json
import os
import shutil
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, confusion_matrix
# ===============================================================
# 1 Copiar JSON a filesDir
# ===============================================================
def ensure_json_in_filesdir(files_dir):
    src_json = os.path.join(os.path.dirname(__file__), "game_data.json")
    dst_json = os.path.join(files_dir, "game_data.json")
    if not os.path.exists(dst_json):
        shutil.copy(src_json, dst_json)
    return dst_json

# ===============================================================
# 2 CARGA Y LIMPIEZA
# ===============================================================
def load_and_clean_from_json(json_path):
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    df = pd.DataFrame(data)
    df["fechaHoraInicio"] = pd.to_datetime(df["fechaHoraInicio"])
    df["fechaHoraFin"] = pd.to_datetime(df["fechaHoraFin"])
    df["gameTime"] = df["gameTime"].fillna(
        (df["fechaHoraFin"] - df["fechaHoraInicio"]).dt.total_seconds()
    )

    df = df[df["gameTime"] > 0]
    df.drop_duplicates(inplace=True)
    return df


# ===============================================================
# 3 VISUALIZACIONES
# ===============================================================
def create_plots(df, files_dir):
    df["puntuacion"] = df["aciertos"] - df["errores"]

    # Marcar jugadores recurrentes
    session_counts = df.groupby("username")["rondas"].count()
    df["returning_player"] = df["username"].map(lambda u: 1 if session_counts[u] > 1 else 0)

    # Histograma duración de sesión
    plt.figure()
    plt.hist(df["gameTime"], bins=20)
    plt.title("Distribución del tiempo de sesión")
    plt.xlabel("Tiempo (s)")
    plt.ylabel("Frecuencia")
    plt.savefig(os.path.join(files_dir, "hist_session_length.png"))

    # Scatter errores vs aciertos
    plt.figure()
    plt.scatter(df["errores"], df["aciertos"])
    plt.title("Errores vs Aciertos")
    plt.xlabel("Errores")
    plt.ylabel("Aciertos")
    plt.savefig(os.path.join(files_dir, "scatter_errors_vs_aciertos.png"))

    # Puntuación media por dificultad
    plt.figure()
    df.groupby("dificultad")["puntuacion"].mean().plot(kind="bar")
    plt.title("Puntuación media por dificultad")
    plt.xlabel("Dificultad")
    plt.ylabel("Puntuación")
    plt.savefig(os.path.join(files_dir, "bar_puntuacion_dificultad.png"))

    # ===============================================================
    # NUEVO 1: Boxplot de tiempo de juego para recurrentes vs no recurrentes
    # ===============================================================
    plt.figure()
    df.boxplot(column="gameTime", by="returning_player")
    plt.title("Duración de sesión: recurrentes vs no recurrentes")
    plt.suptitle("")
    plt.xlabel("Jugador recurrente (1) vs no recurrente (0)")
    plt.ylabel("Tiempo de juego (s)")
    plt.savefig(os.path.join(files_dir, "box_gameTime_recurrentes.png"))

    # ===============================================================
    # NUEVO 2: Media de aciertos y errores por tipo de jugador
    # ===============================================================
    means = df.groupby("returning_player")[["aciertos", "errores"]].mean()

    plt.figure()
    means.plot(kind="bar")
    plt.title("Aciertos y errores promedio: recurrentes vs no recurrentes")
    plt.xlabel("Jugador recurrente (1) vs no recurrente (0)")
    plt.ylabel("Promedio")
    plt.legend(["Aciertos", "Errores"])
    plt.savefig(os.path.join(files_dir, "bar_aciertos_errores_recurrentes.png"))

      # Calcular la probabilidad real de retorno
    prob_matrix = df.groupby(["dificultad", "rondas"])["returning_player"].mean().unstack()

    plt.figure(figsize=(10, 6))
    plt.imshow(prob_matrix, aspect="auto", origin="lower")
    plt.colorbar(label="Probabilidad de retorno")
    plt.title("Probabilidad de retorno según nivel y número de rondas")
    plt.xlabel("Rondas jugadas")
    plt.ylabel("Nivel (dificultad)")
    plt.xticks(range(len(prob_matrix.columns)), prob_matrix.columns)
    plt.yticks(range(len(prob_matrix.index)), prob_matrix.index)
    plt.savefig(os.path.join(files_dir, "heatmap_probabilidad_retorno.png"))
# ===============================================================
# 4 MODELO DE PREDICCIÓN
# ===============================================================
def train_model(df):
    # Marcar jugadores recurrentes
    session_counts = df.groupby("username")["rondas"].count()
    df["returning_player"] = df["username"].map(lambda u: 1 if session_counts[u] > 1 else 0)

    # Asegurar al menos 2 clases
    if df["returning_player"].nunique() < 2:
        print("Advertencia: solo hay una clase, el modelo no se entrenará.")
        return None

    features = ["rondas", "dificultad", "aciertos", "errores", "gameTime"]
    X = df[features]
    y = df["returning_player"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    model = LogisticRegression(max_iter=500)
    model.fit(X_train, y_train)
    preds = model.predict(X_test)

    print("Precisión:", accuracy_score(y_test, preds))
    print("Matriz de confusión:\n", confusion_matrix(y_test, preds))
    return model

# ===============================================================
# 5 FUNCIÓN PRINCIPAL PARA ANDROID
# ===============================================================
def run(files_dir):
    json_path = ensure_json_in_filesdir(files_dir)
    df = load_and_clean_from_json(json_path)
    create_plots(df, files_dir)
    train_model(df)
    return "OK"