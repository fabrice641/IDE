import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import virtualbox
import threading

# Funzione per creare e configurare la macchina virtuale
def create_and_prepare_vm(vm_name, os_type_id, disk_path, disk_size_mb, iso_path, progress_var):
    try:
        # Inizializza VirtualBox
        vbox = virtualbox.VirtualBox()
        
        # Controlla se la macchina virtuale con lo stesso nome già esiste
        if vm_name in [vm.name for vm in vbox.machines]:
            messagebox.showerror("Errore", f"La macchina virtuale '{vm_name}' già esiste.")
            return
        
        vm = vbox.create_machine(name=vm_name, os_type_id=os_type_id, settings_file=None)
        
        # Imposta il gruppo e i flag
        group = ""
        vm.set_group(group)
        flags = 0
        vm.set_flags(flags)
        
        # Registra la macchina virtuale
        vbox.register_machine(vm)
        
        # Crea il disco rigido virtuale
        medium = vbox.create_hard_disk("VDI", disk_path)
        medium.create_base_storage(disk_path, disk_size_mb * 1024 * 1024)
        
        # Aggiungi il disco rigido virtuale alla macchina virtuale
        storage_controller = vm.add_storage_controller("SATA", virtualbox.library.StorageBus.sata)
        storage_controller.attach_device(medium, 0, 0, virtualbox.library.DeviceType.hard_disk)
        
        # Aggiungi il file ISO come drive CD/DVD virtuale
        dvd_controller = vm.add_storage_controller("IDE", virtualbox.library.StorageBus.ide)
        dvd_controller.attach_device(iso_path, 0, 1, virtualbox.library.DeviceType.cd_dvd)
        
        # Avvia la macchina virtuale
        session = vm.create_session()
        progress = vm.launch_vm_process(session, "gui", [])
        
        # Monitora il progresso del processo di avvio
        while not progress.completed:
            progress_var.set(progress.percent)
            session.sleep(1)  # Usa time.sleep(1) per evitare di bloccare il thread
            
        # Controlla se l'avvio è stato completato con successo
        if progress.completed and progress.result_code == virtualbox.library.VBoxErrorCode.vbox_e_success:
            messagebox.showinfo("Successo", f"La macchina virtuale '{vm_name}' è stata creata e avviata con successo.")
        else:
            error_message = f"Errore durante la creazione della macchina virtuale '{vm_name}': {progress.error_info.text}"
            messagebox.showerror("Errore", error_message)
            print(error_message)  # Stampa l'errore nella console per ulteriore debugging
    
    except virtualbox.library.VBoxError as vbox_error:
        error_message = f"Errore di VirtualBox: {str(vbox_error)}"
        messagebox.showerror("Errore", error_message)
        print(error_message)  # Stampa l'errore nella console per ulteriore debugging
    except Exception as e:
        error_message = f"Errore durante l'operazione: {str(e)}"
        messagebox.showerror("Errore", error_message)
        print(error_message)  # Stampa l'errore nella console per ulteriore debugging

# Funzione per gestire l'evento di clic del pulsante
def on_create_vm_button_clicked(entries, progress_var):
    try:
        # Estrai i valori dagli entry
        vm_name = entries['vm_name'].get()
        os_type_id = entries['os_type_id'].get()
        disk_path = entries['disk_path'].get()
        disk_size_mb = int(entries['disk_size_mb'].get())
        iso_path = entries['iso_path'].get()
        
        # Verifica se tutti i valori sono presenti
        if not vm_name or not os_type_id or not disk_path or not iso_path or not disk_size_mb:
            messagebox.showerror("Errore", "Per favore, completa tutti i campi richiesti.")
            return
        
        # Esegui l'operazione in un thread separato per evitare di bloccare l'interfaccia utente
        threading.Thread(target=create_and_prepare_vm, args=(vm_name, os_type_id, disk_path, disk_size_mb, iso_path, progress_var)).start()
    
    except ValueError:
        messagebox.showerror("Errore", "Errore di formato nei dati inseriti.")
    except KeyError:
        messagebox.showerror("Errore", "Uno o più campi mancanti nel dizionario delle entry.")

# Funzione principale per creare l'interfaccia grafica
def main():
    # Crea la finestra principale
    window = tk.Tk()
    window.title("Creazione Macchina Virtuale")
    window.geometry("400x300")

    # Crea un dizionario per memorizzare gli entry
    entries = {}

    # Crea le etichette e gli entry per i parametri
    labels = ["Nome VM:", "Tipo OS ID:", "Percorso disco rigido:", "Dimensione disco rigido (MB):", "Percorso ISO:"]
    entry_keys = ["vm_name", "os_type_id", "disk_path", "disk_size_mb", "iso_path"]
    for i, (label, key) in enumerate(zip(labels, entry_keys)):
        # Crea etichetta
        label_widget = tk.Label(window, text=label)
        label_widget.grid(row=i, column=0, padx=5, pady=5, sticky=tk.W)
        
        # Crea entry o file chooser
        if key == "iso_path":
            # Utilizza un filedialog per scegliere il file ISO
            def select_iso_path():
                iso_path = filedialog.askopenfilename(filetypes=[("ISO files", "*.iso")])
                entries[key].delete(0, tk.END)  # Cancella eventuali dati esistenti
                entries[key].insert(0, iso_path)

            entry = tk.Entry(window)
            entry.grid(row=i, column=1, padx=5, pady=5, sticky=tk.W)
            entry_button = tk.Button(window, text="Seleziona", command=select_iso_path)
            entry_button.grid(row=i, column=2, padx=5, pady=5)
        else:
            entry = tk.Entry(window)
            entry.grid(row=i, column=1, padx=5, pady=5, sticky=tk.W)
        
        # Aggiungi entry al dizionario
        entries[key] = entry
    
    # Crea la progress bar
    progress_var = tk.DoubleVar()
    progress_bar = ttk.Progressbar(window, maximum=100, variable=progress_var, length=300)
    progress_bar.grid(row=len(labels), column=1, columnspan=2, padx=5, pady=5, sticky=tk.W)
    
    # Crea il pulsante per la creazione della VM
    create_vm_button = tk.Button(window, text="Crea Macchina Virtuale", command=lambda: on_create_vm_button_clicked(entries, progress_var))
    create_vm_button.grid(row=len(labels) + 1, column=0, columnspan=3, padx=5, pady=20, sticky=tk.W)

    # Avvia il loop principale di tkinter
    window.mainloop()

# Esegui la funzione principale
if __name__ == "__main__":
    main()
