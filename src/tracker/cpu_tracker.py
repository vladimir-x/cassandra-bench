from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.parse
import threading
import subprocess
import json
import time

PORT = 8081

# Глобальные переменные управления состоянием
is_measuring = False
measurement_thread = None

# Переменные для накопления CPU
cpu_total_sum = 0.0
cpu_count = 0

# Переменные для накопления RAM
ram_total_sum = 0.0
ram_max = 0.0
ram_count = 0

# Предрассчитанный итог
precalculated_result = {
    "status": "success",
    "cpu_average_total": 0.0,
    "ram_average_mb": 0.0,
    "ram_max_mb": 0
}

def measurement_loop():
    """Фоновая функция, которая собирает CPU (через кроссплатформенный JSON) и RAM"""
    global is_measuring, cpu_total_sum, cpu_count, ram_total_sum, ram_max, ram_count, precalculated_result
    print("[LOG] Фоновый цикл измерения запущен.")
    
    while is_measuring:
        # 1. Надежный кроссплатформенный сбор CPU в формате JSON
        # Флаг -o JSON заставляет mpstat отдавать строго стандартизированную структуру
        cmd_cpu = "mpstat -P 1,2,3,4 1 1 -o JSON"
        result_cpu = subprocess.run(cmd_cpu, shell=True, capture_output=True, text=True)
        
        try:
            # Парсим JSON от mpstat
            mpstat_data = json.loads(result_cpu.stdout)
            
            # Извлекаем список метрик по процессорам из блока статистики 'statistics' -> 'cpu-load'
            # Внутри структуры JSON имена полей 'usr' и 'sys' всегда неизменны
            cpu_load_list = mpstat_data['sysstat']['hosts'][0]['statistics'][0]['cpu-load']
            
            
            
            iteration_total_cpu = 0.0
            for core_data in cpu_load_list:
                
               
                
                # Нам нужны только ядра 1, 2, 3, 4 (поле 'cpu' указывает номер ядра)
                if core_data['cpu'] in ['1', '2', '3', '4']:
                    busy = core_data['usr'] + core_data['sys']
                    iteration_total_cpu += busy
                    ## print(f">>> usr: {core_data['usr']}  sys: {core_data['sys']}")
            
            cpu_total_sum += iteration_total_cpu
            cpu_count += 1
            cpu_log_str = f"{iteration_total_cpu:.2f}"
            
        except (json.JSONDecodeError, KeyError, IndexErrors) as e:
            print(f"[ERROR] Не удалось распарсить JSON от mpstat: {e}")
            cpu_log_str = "ERROR"

        # 2. Сбор RAM (free -m)
        result_ram = subprocess.run("free -m | awk '/Mem:/ {print $3}'", shell=True, capture_output=True, text=True)
        ram_str = result_ram.stdout.strip()
        
        try:
            ram_val = float(ram_str)
            ram_total_sum += ram_val
            ram_count += 1
            if ram_val > ram_max:
                ram_max = ram_val
        except ValueError:
            print(f"[ERROR] Не удалось распознать RAM: '{ram_str}'")
        
        # 3. Перерасчет чистого итога на лету
        avg_cpu = cpu_total_sum / cpu_count if cpu_count > 0 else 0.0
        avg_ram = ram_total_sum / ram_count if ram_count > 0 else 0.0
        
        precalculated_result = {
            "status": "success",
            "cpu_average_total": round(avg_cpu, 2),
            "ram_average_mb": round(avg_ram, 2),
            "ram_max_mb": int(ram_max)
        }
        
        print(f"[MONITOR] CPU (1-4): {cpu_log_str}% | RAM: {ram_str} MB. Итог обновлен.")
        time.sleep(0.05)
        
    print("[LOG] Фоновый цикл измерения остановлен.")

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass

    def send_response_json(self, status_code, data_dict):
        self.send_response(status_code)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.end_headers()
        json_output = json.dumps(data_dict, ensure_ascii=False) + "\n"
        self.wfile.write(json_output.encode('utf-8'))

    def do_GET(self):
        global is_measuring, measurement_thread, precalculated_result
        global cpu_total_sum, cpu_count, ram_total_sum, ram_max, ram_count
        
        parsed_url = urllib.parse.urlparse(self.path)
        path = parsed_url.path

        if path == '/start':
            if is_measuring:
                self.send_response_json(400, {"status": "error", "message": "Измерение уже идет!"})
            else:
                print("[LOG] Получен запрос GET /start")
                cpu_total_sum = 0.0
                cpu_count = 0
                ram_total_sum = 0.0
                ram_max = 0.0
                ram_count = 0
                precalculated_result = {
                    "status": "success",
                    "cpu_average_total": 0.0,
                    "ram_average_mb": 0.0,
                    "ram_max_mb": 0
                }
                is_measuring = True
                measurement_thread = threading.Thread(target=measurement_loop)
                measurement_thread.start()
                self.send_response_json(200, {"status": "success", "message": "Measurement started"})

        elif path == '/stop':
            if not is_measuring:
                self.send_response_json(400, {"status": "error", "message": "Measurement was not started"})
                return

            print("[LOG] Получен запрос GET /stop")
            final_response = precalculated_result
            self.send_response_json(200, final_response)
            
            is_measuring = False
            if measurement_thread:
                threading.Thread(target=measurement_thread.join).start()

        else:
            self.send_response_json(404, {"status": "error", "message": "Not Found. Use /start or /stop"})

if __name__ == '__main__':
    server = HTTPServer(('0.0.0.0', PORT), SimpleHTTPRequestHandler)
    print(f"Сервер запущен на порту {PORT}...")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        is_measuring = False
        print("\nСервер остановлен.")
        server.server_close()
