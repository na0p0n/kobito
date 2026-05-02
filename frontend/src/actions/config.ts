"use server";
import { auth } from "@/auth";
import { api } from "@/lib/api";
import { revalidatePath } from "next/cache";

export async function updateWebhookUrl(formData: FormData) {
  const session = await auth();
  if (!session?.user?.id) throw new Error("Unauthorized");
  await api.config.update(session.user.id, {
    discordWebhookUrl: formData.get("discordWebhookUrl") || null,
  });
  revalidatePath("/settings");
}

export async function triggerDigest() {
  const session = await auth();
  if (!session?.user?.id) throw new Error("Unauthorized");
  return api.config.triggerDigest(session.user.id);
}
