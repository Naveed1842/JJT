import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { FirebaseApp, getApp, getApps, initializeApp } from 'firebase/app';
import {
  Auth,
  User,
  getAuth,
  onIdTokenChanged,
  signInWithEmailAndPassword,
  signOut
} from 'firebase/auth';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FirebaseAuthService {
  private app: FirebaseApp | null = null;
  private auth: Auth | null = null;
  private readonly userSubject = new BehaviorSubject<User | null>(null);

  readonly user$ = this.userSubject.asObservable();

  constructor() {
    this.initialize();
  }

  async signIn(email: string, password: string): Promise<void> {
    if (!this.auth) {
      throw new Error('Firebase auth is not configured.');
    }
    await signInWithEmailAndPassword(this.auth, email, password);
  }

  async signOut(): Promise<void> {
    if (!this.auth) {
      return;
    }
    await signOut(this.auth);
  }

  async getIdToken(): Promise<string | null> {
    const user = this.auth?.currentUser;
    if (!user) {
      return null;
    }
    return user.getIdToken();
  }

  async hasAnyRole(roles: string[]): Promise<boolean> {
    const user = this.auth?.currentUser;
    if (!user) {
      return false;
    }
    const token = await user.getIdTokenResult();
    const role = token.claims['role'];
    return typeof role === 'string' && roles.includes(role);
  }

  private initialize(): void {
    const firebaseConfig = environment.firebase;
    if (!firebaseConfig || !firebaseConfig.apiKey) {
      return;
    }

    this.app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);
    this.auth = getAuth(this.app);
    onIdTokenChanged(this.auth, (user) => {
      this.userSubject.next(user);
    });
  }
}

