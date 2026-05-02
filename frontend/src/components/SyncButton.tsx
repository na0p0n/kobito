"use client";
import { useState } from "react";

interface Props {
  lastSyncedAt: string | null;
  onSync: () => Promise<void>;
}

export default function SyncButton({ lastSyncedAt, onSync }: Props) {
  const [syncing, setSyncing] = useState(false);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await onSync();
    } finally {
      setSyncing(false);
    }
  };

  return (
    <div className="flex items-center gap-4">
      <button
        onClick={handleSync}
        disabled={syncing}
        className="px-4 py-2 bg-emerald-600 text-white rounded-lg font-medium disabled:opacity-50 hover:bg-emerald-700 transition"
      >
        {syncing ? "同期中..." : "同期"}
      </button>
      {lastSyncedAt && (
        <span className="text-sm text-gray-500">
          最終同期: {new Date(lastSyncedAt).toLocaleString("ja-JP")}
        </span>
      )}
    </div>
  );
}
