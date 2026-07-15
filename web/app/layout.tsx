import type { Metadata } from "next";
import "./globals.css";
import Script from "next/script";

export const metadata: Metadata = {
  title: "FocalScribe - AI Scriptwriter & Teleprompter for Short-Form Creators",
  description: "Generate viral hooks, high-converting video scripts, SEO titles, captions, and record with perfect eye-contact using our scrolling teleprompter.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="bg-background text-white antialiased">
        {/* Razorpay SDK */}
        <Script
          src="https://checkout.razorpay.com/v1/checkout.js"
          strategy="beforeInteractive"
        />
        
        {/* Google Analytics Script */}
        <Script
          src="https://www.googletagmanager.com/gtag/js?id=G-PLACEHOLDER"
          strategy="afterInteractive"
        />
        <Script id="google-analytics" strategy="afterInteractive">
          {`
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', 'G-PLACEHOLDER', {
              page_path: window.location.pathname,
            });
          `}
        </Script>

        {/* PostHog Tracking Script */}
        <Script id="posthog-analytics" strategy="afterInteractive">
          {`
            !function(t,e){var o,n,p,r;e.__SV||(window.posthog=e,e._i=[],e.init=function(i,s,a){function g(t,e){var o=e.split(".");2==o.length&&(t=t[o[0]],e=o[1]),t[e]=function(){t.push([r].concat(Array.prototype.slice.call(arguments,0)))}}(p=t.createElement("script")).type="text/javascript",p.async=!0,p.src=s.api_host.replace(".js","")+"/static/array.js",(r=t.getElementsByTagName("script")[0]).parentNode.insertBefore(p,r),e._i.push([i,s,a]),e.__SV=1)}(document,window.posthog||[]);
            posthog.init('phc_PLACEHOLDER', {api_host: 'https://us.i.posthog.com'});
          `}
        </Script>
        {children}
      </body>
    </html>
  );
}
