import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div style="
      min-height: 100vh;
      background: #fffdf9;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: 'Hanken Grotesk', sans-serif;
      padding: 40px 24px;
    ">
      <div style="text-align: center; max-width: 480px;">
        <div style="
          font-family: 'IBM Plex Mono', monospace;
          font-size: 13px;
          color: #8a958d;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          margin-bottom: 24px;
        ">404 — Page not found</div>

        <h1 style="
          font-family: 'Newsreader', Georgia, serif;
          font-size: clamp(36px, 8vw, 56px);
          font-weight: 400;
          color: #1c352c;
          line-height: 1.1;
          margin: 0 0 20px;
        ">This page doesn't exist.</h1>

        <p style="
          color: #54625b;
          font-size: 17px;
          line-height: 1.6;
          margin: 0 0 40px;
        ">The page you're looking for may have moved or never existed. Head back home to find what you need.</p>

        <a routerLink="/" style="
          display: inline-block;
          background: #1c352c;
          color: #fff;
          text-decoration: none;
          padding: 14px 32px;
          border-radius: 6px;
          font-size: 15px;
          font-weight: 600;
          letter-spacing: 0.01em;
        ">Back to home</a>
      </div>
    </div>
  `
})
export class NotFoundComponent {}
