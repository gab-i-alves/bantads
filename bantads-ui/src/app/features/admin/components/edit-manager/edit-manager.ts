import { Component, EventEmitter, Input, Output, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-edit-manager',
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-manager.html',
  styleUrl: './edit-manager.css',
})
export class EditManager {
  @Output() fecharModal = new EventEmitter<void>();

  closeModal() {
    this.fecharModal.emit();
  }

}
