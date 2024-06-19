import os
import random
import string

def generate_text_file(file_path, file_size):
    try:
        with open(file_path, 'w') as file:
            remaining_size = file_size
            while remaining_size > 0:
                chunk_size = min(remaining_size, 1024)
                chunk = ''.join(random.choices(string.ascii_letters + ' \n', k=chunk_size))
                file.write(chunk)
                remaining_size -= chunk_size
        # Verifica della dimensione del file
        actual_size = os.path.getsize(file_path)
        if actual_size != file_size:
            print(f"Warning: Expected size {file_size} bytes, but got {actual_size} bytes for {file_path}")
    except Exception as e:
        print(f"Error generating file {file_path}: {e}")

# Ottieni il percorso della cartella in cui si trova lo script
script_dir = os.path.dirname(os.path.abspath(__file__))

# Specifica i percorsi dei file relativi alla cartella dello script
file_sizes = [2*1024, 2*1024*1024, 2*1024*1024*1024]  # 2KB, 2MB, 2GB

for size in file_sizes:
    file_path = os.path.join(script_dir, f"file_{size}.txt")
    print(f"Generating file: {file_path} with size {size} bytes")
    generate_text_file(file_path, size)
    print(f"File generated: {file_path}")
