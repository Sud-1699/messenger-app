import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        redirectTo: '/login',
        pathMatch: 'full'
    },
    {
        title: 'Login',
        path: 'login',
        loadComponent: () => import("./login/login").then(c => c.Login)
    },
    {
        title: 'Register',
        path: 'register',
        loadComponent: () => import("./registration/registration").then(c => c.Registration)
    }
];
