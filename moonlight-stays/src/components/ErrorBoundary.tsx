"use client";

import { Component, type ReactNode } from "react";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error("ErrorBoundary caught:", error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback ?? (
          <div
            className="min-h-screen flex flex-col items-center justify-center p-8"
            style={{ background: "#0a0a1a", color: "#fff" }}
          >
            <h1 className="text-xl font-bold mb-4">Something went wrong</h1>
            <p className="text-white/70 mb-6 text-center max-w-md">
              The app encountered an error. Try refreshing the page.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 rounded-xl bg-[#00FFFF]/20 border border-[#00FFFF]/50 text-[#00FFFF] font-medium"
            >
              Refresh
            </button>
          </div>
        )
      );
    }
    return this.props.children;
  }
}
