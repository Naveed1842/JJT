import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { RoleService, UserRole } from '../../services/role.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  currentRole: UserRole = 'ORG_ADMIN';

  constructor(private roleService: RoleService) {}

  ngOnInit() {
    this.currentRole = this.roleService.getRole();
  }

  onRoleChange() {
    this.roleService.setRole(this.currentRole);
  }
}
