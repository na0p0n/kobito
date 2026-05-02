"use client";
import { useState } from "react";
import { triggerDigest } from "@/actions/config";

export default function DigestTestButton() {
  const [status, setStatus] = useState<"idle" | "sending" | "done" | "skipped">("idle");

  const handleClick = async () => {
    setStatus("sending");
    const result = await triggerDigest();
    setStatus(result.sent ? "done" : "skipped");
    setTimeout(() => setStatus("idle"), 3000);
  };

  return (
    <div className="flex items-center gap-4">
      <button
        onClick={handleClick}
        disabled={status === "sending"}
        className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-100 transition disabled:opacity-50"
      >
        {status === "sending" ? "送信中..." : "テスト送信"}
      </button>
      {status === "done" && <span className="text-sm text-emerald-600">送信しました</span>}
      {status === "skipped" && (
        <span className="text-sm text-gray-500">Webhook URL が未設定のためスキップしました</span>
      )}
    </div>
  );
}
