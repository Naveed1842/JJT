import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';
import { filter } from 'rxjs/operators';

interface NavChild {
  label: string;
  route: string;
  queryParams?: Record<string, string>;
}

interface NavItem {
  label: string;
  icon: string;
  route?: string;
  children?: NavChild[];
  expanded?: boolean;
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  sidebarOpen = true;
  isMobile = false;
  private resizeHandler = () => this.checkMobile();

  navGroups: NavGroup[] = [
    {
      label: 'Overview',
      items: [
        { label: 'Dashboard', icon: 'DB', route: 'dashboard' }
      ]
    },
    {
      label: 'Directory',
      items: [
        { label: 'Children', icon: 'CH', route: 'children' },
        { label: 'Sponsors', icon: 'SP', route: 'sponsors' }
      ]
    },
    {
      label: 'Operations',
      items: [
        { label: 'Early Support', icon: 'ES', route: 'early-support' },
        { label: 'Progress Updates', icon: 'PR', route: 'progress' }
      ]
    },
    {
      label: 'Sponsorships',
      items: [
        {
          label: 'Sponsorships',
          icon: 'SS',
          route: 'sponsorships',
          expanded: true,
          children: [
            { label: 'Create', route: 'sponsorships', queryParams: { tab: 'create' } },
            { label: 'Pending', route: 'sponsorships', queryParams: { tab: 'pending' } },
            { label: 'Active', route: 'sponsorships', queryParams: { tab: 'active' } }
          ]
        }
      ]
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    this.checkMobile();
    this.updateActiveNavItem(this.router.url);

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateActiveNavItem(event.urlAfterRedirects);
      });

    window.addEventListener('resize', this.resizeHandler);
  }

  ngOnDestroy() {
    window.removeEventListener('resize', this.resizeHandler);
  }

  checkMobile() {
    this.isMobile = window.innerWidth < 768;
    if (this.isMobile) {
      this.sidebarOpen = false;
    }
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebarOnMobile() {
    if (this.isMobile) {
      this.sidebarOpen = false;
    }
  }

  updateActiveNavItem(url: string) {
    this.navGroups.forEach(group => {
      group.items.forEach(item => {
        const childActive = (item.children || []).some(child => this.isChildActive(child, url));
        if (childActive) {
          item.expanded = true;
        }
      });
    });
  }

  isRouteActive(route?: string, url?: string) {
    if (!route || !url) {
      return false;
    }
    return url.includes(`/admin/${route}`);
  }

  isChildActive(child: NavChild, url?: string) {
    if (!url) {
      return false;
    }
    const routeMatch = url.includes(`/admin/${child.route}`);
    // const queryMatch = child.queryParams?.tab ? url.includes(`tab=${child.queryParams.tab}`) : true;
    return routeMatch;
  }

  onItemClick(item: NavItem) {
    if (item.route) {
      this.router.navigate([`/admin/${item.route}`]);
      this.closeSidebarOnMobile();
      return;
    }
    if (item.children) {
      item.expanded = !item.expanded;
    }
  }

  toggleItem(item: NavItem, event: MouseEvent) {
    event.stopPropagation();
    item.expanded = !item.expanded;
  }

  navigateToChild(child: NavChild) {
    this.router.navigate([`/admin/${child.route}`], { queryParams: child.queryParams || {} });
    this.closeSidebarOnMobile();
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateTo(route: string) {
    this.router.navigate([`/admin/${route}`]);
    this.closeSidebarOnMobile();
  }
}
