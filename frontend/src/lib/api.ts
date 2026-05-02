const API_BASE = process.env.BACKEND_URL ?? "http://backend:8080";

async function backendFetch<T>(
  path: string,
  userId: string,
  options: RequestInit = {}
): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      "X-User-Id": userId,
      ...options.headers,
    },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`Backend error: ${res.status}`);
  return res.json();
}

export type ContributionType = "COMMIT" | "PR" | "ISSUE" | "REVIEW";

export interface GoalWithProgress {
  id: number;
  title: string;
  contributionType: ContributionType;
  targetCount: number;
  startDate: string;
  endDate: string;
  currentCount: number;
  achieved: boolean;
}

export interface WeeklyTotal {
  commit: number;
  pr: number;
  issue: number;
  review: number;
}

export interface ConfigView {
  discordWebhookUrl: string | null;
  digestSendDay: string;
  digestSendHour: number;
}

export const api = {
  goals: {
    list: (userId: string) =>
      backendFetch<{ goals: GoalWithProgress[] }>("/api/goals", userId),
    create: (userId: string, body: object) =>
      backendFetch<GoalWithProgress>("/api/goals", userId, {
        method: "POST",
        body: JSON.stringify(body),
      }),
    delete: (userId: string, id: number) =>
      backendFetch<void>(`/api/goals/${id}`, userId, { method: "DELETE" }),
  },
  contributions: {
    weekly: (userId: string) =>
      backendFetch<{ week: WeeklyTotal }>("/api/contributions/weekly", userId),
  },
  sync: {
    trigger: (userId: string, accessToken: string) =>
      backendFetch<{ syncedAt: string; totalRecords: number }>("/api/sync", userId, {
        method: "POST",
        headers: { "X-Access-Token": accessToken },
      }),
    status: (userId: string) =>
      backendFetch<{ lastSyncedAt: string | null }>("/api/sync/status", userId),
  },
  config: {
    get: (userId: string) =>
      backendFetch<ConfigView>("/api/config", userId),
    update: (userId: string, body: object) =>
      backendFetch<ConfigView>("/api/config", userId, {
        method: "PUT",
        body: JSON.stringify(body),
      }),
    triggerDigest: (userId: string) =>
      backendFetch<{ sent: boolean }>("/api/digest/trigger", userId, { method: "POST" }),
  },
};
