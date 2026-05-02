"use client";
import { useRef } from "react";
import { createGoal } from "@/actions/goals";

export default function GoalCreateForm() {
  const ref = useRef<HTMLFormElement>(null);

  const handleSubmit = async (formData: FormData) => {
    await createGoal(formData);
    ref.current?.reset();
  };

  return (
    <form ref={ref} action={handleSubmit} className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-base font-semibold text-gray-700 mb-4">新しいゴールを作成</h2>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="text-sm font-medium text-gray-600" htmlFor="title">タイトル</label>
          <input
            id="title"
            name="title"
            required
            className="mt-1 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-600" htmlFor="contributionType">種別</label>
          <select
            id="contributionType"
            name="contributionType"
            className="mt-1 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          >
            <option value="COMMIT">Commit</option>
            <option value="PR">PR</option>
            <option value="ISSUE">Issue</option>
            <option value="REVIEW">Review</option>
          </select>
        </div>
        <div>
          <label className="text-sm font-medium text-gray-600" htmlFor="targetCount">目標件数</label>
          <input
            id="targetCount"
            name="targetCount"
            type="number"
            min="1"
            required
            defaultValue={10}
            className="mt-1 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-600" htmlFor="startDate">開始日</label>
          <input
            id="startDate"
            name="startDate"
            type="date"
            required
            className="mt-1 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          />
        </div>
        <div>
          <label className="text-sm font-medium text-gray-600" htmlFor="endDate">終了日</label>
          <input
            id="endDate"
            name="endDate"
            type="date"
            required
            className="mt-1 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          />
        </div>
      </div>
      <button
        type="submit"
        className="mt-4 px-5 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition"
      >
        作成
      </button>
    </form>
  );
}
