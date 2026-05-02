"use server";
import { auth } from "@/auth";
import { api } from "@/lib/api";
import { revalidatePath } from "next/cache";

export async function createGoal(formData: FormData) {
  const session = await auth();
  if (!session?.user?.id) throw new Error("Unauthorized");

  await api.goals.create(session.user.id, {
    title: formData.get("title"),
    contributionType: formData.get("contributionType"),
    targetCount: Number(formData.get("targetCount")),
    startDate: formData.get("startDate"),
    endDate: formData.get("endDate"),
  });
  revalidatePath("/goals");
}

export async function deleteGoal(id: number) {
  const session = await auth();
  if (!session?.user?.id) throw new Error("Unauthorized");
  await api.goals.delete(session.user.id, id);
  revalidatePath("/goals");
}
