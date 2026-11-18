import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './auth/login.component';
import { ItWorksComponent } from './main/it-works.component';
import { WorkersComponent } from './main/workers.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'it-works', component: ItWorksComponent, canActivate: [authGuard] },
	{ path: 'workers', component: WorkersComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: '**', redirectTo: 'login' }
];
